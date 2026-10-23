package gragongit.arcaneartistry.client.model.crystal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.joml.Vector3dc;
import org.jspecify.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.block.CrystalBlock;
import gragongit.arcaneartistry.common.block.crystal.CrystalGeometry;
import gragongit.arcaneartistry.common.block.crystal.Polygon;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block model of a {@link CrystalBlock}. The geometry of each crystal depends on the surrounding crystals and is computed by
 * {@link CrystalGeometry}. Meshes are cached per neighbourhood.
 */
public final class CrystalBlockModel implements BlockStateModel {
  public static final Identifier TYPE_ID = ArcaneArtistry.id("crystal");

  private static final AtomicBoolean REGISTERED = new AtomicBoolean();
  private static final int MAX_CACHED_CELLS = 16384;

  private final Material.Baked material;
  private final Map<CrystalGeometry.Key, CellMesh> cells = new ConcurrentHashMap<>();
  private final BlockStateModelPart isolatedPart;

  private CrystalBlockModel(Material.Baked material) {
    this.material = material;
    CrystalGeometry.Key isolatedKey = CrystalGeometry.key((x, y, z) -> x == 0 && y == 0 && z == 0 ? CrystalBlock.CRYSTAL : CrystalBlock.EMPTY, true);
    this.isolatedPart = new SimpleModelWrapper(new MeshQuadCollection(cell(isolatedKey).mesh()), false, material);
  }

  /**
   * Registers the {@code arcane-artistry:crystal} block state model type. Safe to call multiple times.
   */
  public static void register() {
    if (REGISTERED.compareAndSet(false, true)) {
      CustomUnbakedBlockStateModel.register(TYPE_ID, Unbaked.CODEC);
    }
  }

  private record CellMesh(Mesh mesh, long[][] rubbleMargins) {}

  private CellMesh cell(CrystalGeometry.Key key) {
    CellMesh cell = cells.get(key);
    if (cell == null) {
      if (cells.size() > MAX_CACHED_CELLS) {
        cells.clear();
      }
      CrystalGeometry.Result result = CrystalGeometry.build(key);
      cell = new CellMesh(bake(result.surface()), result.rubbleMargins());
      cells.put(key, cell);
    }
    return cell;
  }

  private Mesh bake(List<Polygon> surface) {
    MutableMesh mesh = Renderer.get().mutableMesh();
    QuadEmitter emitter = mesh.emitter();
    for (Polygon polygon : surface) {
      Direction face = Direction.getApproximateNearest(polygon.normal().x(), polygon.normal().y(), polygon.normal().z());
      Direction cullFace = isOnBorder(polygon, face) ? face : null;
      List<Vector3dc> vertices = polygon.vertices();
      for (int start = 1; start + 1 < vertices.size(); start += 2) {
        for (int i = 0; i < 4; i++) {
          Vector3dc vertex = vertices.get(i == 0 ? 0 : Math.min(start + i - 1, vertices.size() - 1));
          float x = (float) vertex.x() / CrystalGeometry.SIZE;
          float y = (float) vertex.y() / CrystalGeometry.SIZE;
          float z = (float) vertex.z() / CrystalGeometry.SIZE;
          emitter.pos(i, x, y, z);
          setLockedUv(emitter, i, face, x, y, z);
        }
        emitter
            .nominalFace(face)
            .cullFace(cullFace)
            .ambientOcclusion(TriState.FALSE)
            .materialBake(material, MutableQuadView.BAKE_NORMALIZED)
            .emit();
      }
    }
    return mesh.immutableCopy();
  }

  private static boolean isOnBorder(Polygon polygon, Direction face) {
    if (Math.abs(polygon.normal().get(face.getAxis().ordinal())) < 1 - Polygon.EPSILON) {
      return false;
    }
    double border = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? CrystalGeometry.SIZE : 0;
    return Math.abs(polygon.vertices().getFirst().get(face.getAxis().ordinal()) - border) < Polygon.EPSILON;
  }

  /**
   * Assigns texture coordinates like a vanilla face with uv lock, so the texture lines up across neighbouring crystals.
   */
  static void setLockedUv(QuadEmitter emitter, int vertex, Direction face, float x, float y, float z) {
    switch (face) {
      case DOWN -> emitter.uv(vertex, x, 1 - z);
      case UP -> emitter.uv(vertex, x, z);
      case NORTH -> emitter.uv(vertex, 1 - x, 1 - y);
      case SOUTH -> emitter.uv(vertex, x, 1 - y);
      case WEST -> emitter.uv(vertex, z, 1 - y);
      case EAST -> emitter.uv(vertex, 1 - z, 1 - y);
    }
  }

  @Override
  public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
      Predicate<@Nullable Direction> cullTest) {
    CrystalGeometry.Key key = CrystalGeometry.key(new CrystalGeometry.LevelCells(level, pos, state.getBlock()), true);
    CellMesh cell = cell(key);
    cell.mesh().outputTo(emitter);
    CrystalRubble.emit(emitter, level, pos, state, cell.rubbleMargins(), random);
  }

  @Override
  public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
    output.add(isolatedPart);
  }

  @Override
  public Material.Baked particleMaterial() {
    return material;
  }

  @Override
  public @BakedQuad.MaterialFlags int materialFlags() {
    // Rubble uses the textures of neighbouring blocks, which may be animated.
    return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED;
  }

  public record Unbaked(Identifier texture) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder
        .mapCodec(instance -> instance.group(Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture)).apply(instance, Unbaked::new));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
      return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {}

    @Override
    public BlockStateModel bake(ModelBaker modelBakery) {
      return new CrystalBlockModel(modelBakery.materials().get(new Material(texture, true), () -> TYPE_ID + " " + texture));
    }
  }
}
