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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block model of a {@link CrystalBlock}. The geometry of each crystal depends on the surrounding crystals and is computed by
 * {@link CrystalGeometry}. Meshes are cached per neighbourhood.
 *
 * <p>
 * Each side of a crystal shimmers with a chance of one in {@code shimmer_chance}, by default {@value #DEFAULT_SHIMMER_CHANCE}, showing one
 * of the shimmer textures instead of the normal one. Slopes belong to the side they mostly face.
 */
public final class CrystalBlockModel implements BlockStateModel {
  public static final Identifier TYPE_ID = ArcaneArtistry.id("crystal");

  private static final AtomicBoolean REGISTERED = new AtomicBoolean();
  private static final int MAX_CACHED_CELLS = 16384;
  private static final int DEFAULT_SHIMMER_CHANCE = 16;

  private final Material.Baked material;
  private final List<Material.Baked> shimmerMaterials;
  private final int shimmerChance;
  private final Map<CrystalGeometry.Key, CellMesh> cells = new ConcurrentHashMap<>();
  private final BlockStateModelPart isolatedPart;

  private CrystalBlockModel(Material.Baked material, List<Material.Baked> shimmerMaterials, int shimmerChance) {
    this.material = material;
    this.shimmerMaterials = shimmerMaterials;
    this.shimmerChance = shimmerChance;
    CrystalGeometry.Key isolatedKey =
        CrystalGeometry.key((x, y, z) -> x == 0 && y == 0 && z == 0 ? CrystalBlock.CRYSTAL : CrystalBlock.EMPTY, true);
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

  private record CellMesh(Mesh mesh, long[][] rubbleMargins) {
  }

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
  static void setLockedUv(MutableQuadView emitter, int vertex, Direction face, float x, float y, float z) {
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
    Material.Baked[] shimmer = shimmer(pos, state, random);
    if (shimmer == null) {
      cell.mesh().outputTo(emitter);
    } else {
      emitter.pushTransform(quad -> {
        Material.Baked sideMaterial = shimmer[quad.nominalFace().ordinal()];
        if (sideMaterial != null) {
          for (int i = 0; i < 4; i++) {
            setLockedUv(quad, i, quad.nominalFace(), quad.x(i), quad.y(i), quad.z(i));
          }
          quad.materialBake(sideMaterial, MutableQuadView.BAKE_NORMALIZED);
        }
        return true;
      });
      cell.mesh().outputTo(emitter);
      emitter.popTransform();
    }
    CrystalRubble.emit(emitter, level, pos, state, cell.rubbleMargins(), random);
  }

  /**
   * Picks the shimmer material of each side of the crystal at {@code pos}, null for sides with the normal texture. Returns null if no side
   * shimmers.
   */
  private Material.Baked @Nullable [] shimmer(BlockPos pos, BlockState state, RandomSource random) {
    if (shimmerMaterials.isEmpty()) {
      return null;
    }
    // Seeded apart from the debris, which uses the seeds of the six sides below.
    random.setSeed(state.getSeed(pos) * 31 + Direction.values().length);
    Material.Baked[] materials = new Material.Baked[Direction.values().length];
    boolean shimmers = false;
    for (Direction side : Direction.values()) {
      int roll = random.nextInt(shimmerChance * shimmerMaterials.size());
      if (roll < shimmerMaterials.size()) {
        materials[side.ordinal()] = shimmerMaterials.get(roll);
        shimmers = true;
      }
    }
    return shimmers ? materials : null;
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
    // The shimmer textures and the textures of neighbouring blocks used by the rubble may be animated.
    return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED;
  }

  public record Unbaked(Identifier texture, List<Identifier> shimmerTextures, int shimmerChance) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder
        .mapCodec(instance -> instance
            .group(Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture),
                Identifier.CODEC.listOf().optionalFieldOf("shimmer_textures", List.of()).forGetter(Unbaked::shimmerTextures),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("shimmer_chance", DEFAULT_SHIMMER_CHANCE).forGetter(Unbaked::shimmerChance))
            .apply(instance, Unbaked::new));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
      return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {}

    @Override
    public BlockStateModel bake(ModelBaker modelBakery) {
      return new CrystalBlockModel(material(modelBakery, texture),
          shimmerTextures.stream().map(shimmerTexture -> material(modelBakery, shimmerTexture)).toList(), shimmerChance);
    }

    private static Material.Baked material(ModelBaker modelBakery, Identifier texture) {
      return modelBakery.materials().get(new Material(texture, true), () -> TYPE_ID + " " + texture);
    }
  }
}
