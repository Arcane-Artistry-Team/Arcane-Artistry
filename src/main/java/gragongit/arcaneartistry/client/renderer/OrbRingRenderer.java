package gragongit.arcaneartistry.client.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.block.OrbRingBlock;
import gragongit.arcaneartistry.common.block.OrbRingBlockEntity;
import gragongit.arcaneartistry.common.registry.ModBlockEntities;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class OrbRingRenderer implements BlockEntityRenderer<OrbRingBlockEntity, OrbRingRenderState> {
  private static final Identifier CRYSTAL_MODEL_ID = ArcaneArtistry.id("block/lapis_crystal");
  private static final ExtraModelKey<BlockStateModel> CRYSTAL_MODEL = ExtraModelKey.create(CRYSTAL_MODEL_ID::toString);

  private static final float PIXEL = 1F / 16F;
  private static final float LIFT = 3 * PIXEL;
  private static final float HOVER_AMPLITUDE = PIXEL / 2F;
  private static final int HOVER_PERIOD_TICKS = 80;

  private static final Vector3fc CRYSTAL_CENTER = new Vector3f(8, 7, 8).mul(PIXEL);
  private static final float GALAXY_INSET = 0.02F * PIXEL;
  private static final List<Vector3fc> GALAXY_VERTICES = Stream
      .of(galaxyBox(5, 5, 6, 11, 9, 10), galaxyBox(6, 5, 5, 10, 9, 11), galaxyBox(6, 4, 6, 10, 10, 10))
      .flatMap(List::stream)
      .toList();

  private final RandomSource random = RandomSource.create();

  public static void register() {
    ModelLoadingPlugin.register(context -> context.addModel(CRYSTAL_MODEL, SimpleUnbakedExtraModel.blockStateModel(CRYSTAL_MODEL_ID)));
    BlockEntityRenderers.register(ModBlockEntities.ORB_RING, context -> new OrbRingRenderer());
  }

  private static List<Vector3fc> galaxyBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    Vector3f min = new Vector3f(minX, minY, minZ).mul(PIXEL).add(GALAXY_INSET, GALAXY_INSET, GALAXY_INSET);
    Vector3f max = new Vector3f(maxX, maxY, maxZ).mul(PIXEL).sub(GALAXY_INSET, GALAXY_INSET, GALAXY_INSET);
    List<Vector3fc> vertices = new ArrayList<>();
    for (Direction direction : Direction.values()) {
      FaceInfo face = FaceInfo.fromFacing(direction);
      for (int i = 0; i < 4; i++) {
        vertices.add(face.getVertexInfo(i).select(min, max));
      }
    }
    return vertices;
  }

  @Override
  public OrbRingRenderState createRenderState() {
    return new OrbRingRenderState();
  }

  @Override
  public void extractRenderState(OrbRingBlockEntity orbRing, OrbRingRenderState state, float partialTicks, Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(orbRing, state, partialTicks, cameraPosition, breakProgress);
    state.hasCrystal = orbRing.getBlockState().getValue(OrbRingBlock.CRYSTAL);
    state.activation = Mth.smoothstep(orbRing.getActivation(partialTicks));
    state.spinAngle = orbRing.getSpinAngle(partialTicks);

    Level level = orbRing.getLevel();
    long gameTime = level != null ? level.getGameTime() : 0;
    state.hoverPhase = ((gameTime % HOVER_PERIOD_TICKS) + partialTicks) / HOVER_PERIOD_TICKS * Mth.TWO_PI;
  }

  @Override
  public void submit(OrbRingRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (!state.hasCrystal) {
      return;
    }

    BlockStateModel model = Minecraft.getInstance().getModelManager().getModel(CRYSTAL_MODEL);
    if (model == null) {
      return;
    }

    poseStack.pushPose();
    poseStack.translate(0, state.activation * (LIFT + Mth.sin(state.hoverPhase) * HOVER_AMPLITUDE), 0);
    poseStack.rotateAround(Axis.YP.rotation(state.spinAngle), CRYSTAL_CENTER.x(), CRYSTAL_CENTER.y(), CRYSTAL_CENTER.z());

    if (state.activation > 0) {
      submitGalaxy(state.activation, poseStack, submitNodeCollector);
    }

    List<BlockStateModelPart> parts = new ArrayList<>();
    model.collectParts(random, parts);
    submitNodeCollector
        .submitBlockModel(poseStack, Sheets.translucentBlockItemSheet(), parts, BlockModelRenderState.EMPTY_TINTS, state.lightCoords,
            OverlayTexture.NO_OVERLAY, 0);

    poseStack.popPose();
  }

  private static void submitGalaxy(float scale, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
    poseStack.pushPose();
    poseStack.translate(CRYSTAL_CENTER.x(), CRYSTAL_CENTER.y(), CRYSTAL_CENTER.z());
    poseStack.scale(scale, scale, scale);
    poseStack.translate(-CRYSTAL_CENTER.x(), -CRYSTAL_CENTER.y(), -CRYSTAL_CENTER.z());
    submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.endPortal(), (pose, buffer) -> {
      for (Vector3fc vertex : GALAXY_VERTICES) {
        buffer.addVertex(pose, vertex);
      }
    });
    poseStack.popPose();
  }
}
