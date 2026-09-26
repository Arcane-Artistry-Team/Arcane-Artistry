package gragongit.arcaneartistry.client.crystalball;

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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

public final class CrystalBallGalaxy {
  private static final float TIME_SCALE = 0.4f;
  private static final float TIME_OFFSET = 40f;
  private static final float GALAXY_SIZE = 600f;
  private static final float PIXEL_SIZE = 2f;

  private static final VertexFormat FORMAT = VertexFormat
      .builder(0)
      .addAttribute("Position", GpuFormat.RGB32_FLOAT)
      .addAttribute("UV0", GpuFormat.RG32_FLOAT)
      .addAttribute("UV3", GpuFormat.RG32_FLOAT)
      .build();

  private static final RenderPipeline PIPELINE = RenderPipeline
      .builder(RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(ArcaneArtistry.id("pipeline/crystal_ball_galaxy"))
      .withBindGroupLayout(BindGroupLayouts.PROJECTION)
      .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
      .withVertexShader(ArcaneArtistry.id("core/crystal_ball_galaxy"))
      .withFragmentShader(ArcaneArtistry.id("core/crystal_ball_galaxy"))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withVertexBinding(0, FORMAT)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .build();

  private CrystalBallGalaxy() {}

  public static void register() {
    RenderPipelines.register(PIPELINE);
  }

  public static void render(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float rootX, float rootY, float zoom,
      float seconds) {
    float scale = GALAXY_SIZE * zoom;
    float u0 = (x0 - rootX) / scale;
    float u1 = (x1 - rootX) / scale;
    float v0 = (y0 - rootY) / scale;
    float v1 = (y1 - rootY) / scale;
    float time = TIME_OFFSET + seconds * TIME_SCALE;
    float pixel = PIXEL_SIZE / scale;
    graphics.guiRenderState.addGuiElement(new State(new Matrix3x2f(graphics.pose()), x0, y0, x1, y1, u0, u1, v0, v1, time, pixel));
  }

  private record State(Matrix3x2fc pose, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, float time, float pixel,
      @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

    State(Matrix3x2f pose, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, float time, float pixel) {
      this(pose, x0, y0, x1, y1, u0, u1, v0, v1, time, pixel, new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
      consumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setUv3(time, pixel);
      consumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setUv3(time, pixel);
      consumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setUv3(time, pixel);
      consumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setUv3(time, pixel);
    }

    @Override
    public RenderPipeline pipeline() {
      return PIPELINE;
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
