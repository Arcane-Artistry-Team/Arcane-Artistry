package gragongit.arcaneartistry.client.crystalball;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class CrystalBallBackground {
  public static final Identifier DEFAULT_SHADER = ArcaneArtistry.id("crystal_ball/spiral_galaxy");
  private static final Identifier VERTEX_SHADER = ArcaneArtistry.id("crystal_ball/background");

  private static final float TIME_OFFSET = 100f;
  private static final double EDGE_UV = 0.65;
  private static final double DEEPEST_FOCUS_ZOOM = 10;
  public static final int NO_REVEAL_MASK = Short.MAX_VALUE;

  private static final VertexFormat FORMAT = VertexFormat
      .builder(0)
      .addAttribute("Position", GpuFormat.RGB32_FLOAT)
      .addAttribute("UV0", GpuFormat.RG32_FLOAT)
      .addAttribute("UV3", GpuFormat.RG32_FLOAT)
      .addAttribute("UV1", GpuFormat.RG16_SINT)
      .addAttribute("UV2", GpuFormat.RG16_SINT)
      .build();

  private static final Map<Identifier, RenderPipeline> PIPELINES = new HashMap<>();
  private static final Set<Identifier> WARNED_MISSING = new HashSet<>();

  private CrystalBallBackground() {}

  public static void register() {
    RenderPipelines.register(pipelineFor(DEFAULT_SHADER));
  }

  private static RenderPipeline pipelineFor(Identifier fragmentShader) {
    return PIPELINES.computeIfAbsent(fragmentShader, CrystalBallBackground::createPipeline);
  }

  private static RenderPipeline createPipeline(Identifier fragmentShader) {
    return RenderPipeline
        .builder(RenderPipelines.GLOBALS_SNIPPET)
        .withLocation(
            ArcaneArtistry.id("pipeline/crystal_ball_background/" + fragmentShader.getNamespace() + "/" + fragmentShader.getPath()))
        .withBindGroupLayout(BindGroupLayouts.PROJECTION)
        .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
        .withVertexShader(VERTEX_SHADER)
        .withFragmentShader(fragmentShader)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexBinding(0, FORMAT)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .build();
  }

  private static RenderPipeline resolvePipeline(Identifier fragmentShader) {
    RenderPipeline pipeline = PIPELINES.get(fragmentShader);
    if (pipeline != null) {
      return pipeline;
    }
    Identifier file = fragmentShader.withPath(path -> "shaders/" + path + ".fsh");
    if (Minecraft.getInstance().getResourceManager().getResource(file).isEmpty()) {
      if (WARNED_MISSING.add(fragmentShader)) {
        ArcaneArtistry.LOGGER.warn("Crystal ball background {} not found, using {}", file, DEFAULT_SHADER);
      }
      return pipelineFor(DEFAULT_SHADER);
    }
    return pipelineFor(fragmentShader);
  }

  public static double parallaxZoom(double zoom, double rootFocusZoom, double deepestFocusZoom) {
    double depthRange = Math.log(deepestFocusZoom / rootFocusZoom);
    double exponent = depthRange > 0 ? Math.log(DEEPEST_FOCUS_ZOOM) / depthRange : 0;
    return rootFocusZoom * Math.pow(zoom / rootFocusZoom, exponent);
  }

  public static void render(GuiGraphicsExtractor graphics, Identifier fragmentShader, int x0, int y0, int x1, int y1, double rootX,
      double rootY, double zoom, double radius, float seconds, double revealX, double revealY, double revealRadius) {
    double scale = radius / EDGE_UV * zoom;
    float u0 = (float) ((x0 - rootX) / scale);
    float u1 = (float) ((x1 - rootX) / scale);
    float v0 = (float) ((y0 - rootY) / scale);
    float v1 = (float) ((y1 - rootY) / scale);
    float time = TIME_OFFSET + seconds;
    float pixel = (float) (1 / scale);
    Reveal reveal = new Reveal(Mth.floor(revealX - rootX), Mth.floor(revealY - rootY), (int) Math.min(revealRadius, NO_REVEAL_MASK));
    graphics.guiRenderState
        .addGuiElement(new State(resolvePipeline(fragmentShader), new Matrix3x2f(graphics.pose()), x0, y0, x1, y1, u0, u1, v0, v1, time,
            pixel, reveal));
  }

  private record Reveal(int x, int y, int radius) {
  }

  private record State(RenderPipeline pipeline, Matrix3x2fc pose, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1,
      float time, float pixel, Reveal reveal, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

    State(RenderPipeline pipeline, Matrix3x2f pose, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, float time,
        float pixel, Reveal reveal) {
      this(pipeline, pose, x0, y0, x1, y1, u0, u1, v0, v1, time, pixel, reveal,
          new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
      vertex(consumer, x0, y0, u0, v0);
      vertex(consumer, x0, y1, u0, v1);
      vertex(consumer, x1, y1, u1, v1);
      vertex(consumer, x1, y0, u1, v0);
    }

    private void vertex(VertexConsumer consumer, int x, int y, float u, float v) {
      consumer.addVertexWith2DPose(pose, x, y).setUv(u, v).setUv3(time, pixel).setUv1(reveal.x(), reveal.y()).setUv2(reveal.radius(), 0);
    }

    @Override
    public TextureSetup textureSetup() {
      return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
      return null;
    }
  }
}
