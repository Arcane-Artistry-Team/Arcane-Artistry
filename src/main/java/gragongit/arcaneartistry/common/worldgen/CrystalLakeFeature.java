package gragongit.arcaneartistry.common.worldgen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * A flat lake of crystal sunk into the floor, with its surface level with the floor around it. It is a few overlapping ellipses of up to
 * {@code radius} blocks. It stays one block deep for {@code shelf_width} blocks from its shore and then gets one block deeper every
 * {@code slope_width} blocks towards its middle, up to {@code max_depth}. Its origin is the air block above the floor; lakes on floors
 * with too many holes are skipped. The crystal replaces air, water and {@code replaceable} blocks.
 */
public record CrystalLakeFeature(Holder<BlockStateProvider> crystal, IntProvider radius, IntProvider maxDepth, IntProvider shelfWidth,
    IntProvider slopeWidth, HolderSet<Block> replaceable) implements Feature {
  public static final MapCodec<CrystalLakeFeature> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(BlockStateProvider.CODEC.fieldOf("crystal").forGetter(CrystalLakeFeature::crystal),
              IntProviders.codec(1, 15).fieldOf("radius").forGetter(CrystalLakeFeature::radius),
              IntProviders.codec(1, 8).fieldOf("max_depth").forGetter(CrystalLakeFeature::maxDepth),
              IntProviders.codec(1, 15).fieldOf("shelf_width").forGetter(CrystalLakeFeature::shelfWidth),
              IntProviders.codec(1, 15).fieldOf("slope_width").forGetter(CrystalLakeFeature::slopeWidth),
              RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("replaceable").forGetter(CrystalLakeFeature::replaceable))
          .apply(instance, CrystalLakeFeature::new));

  /**
   * The largest share of the lake's columns whose floor may be missing.
   */
  private static final float MAX_HOLES = 0.1F;

  @Override
  public MapCodec<CrystalLakeFeature> codec() {
    return CODEC;
  }

  @Override
  public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
    if (!level.getBlockState(origin).isAir()) {
      return false;
    }
    int lakeRadius = radius.sample(random);
    int[] distances = shoreDistances(shape(lakeRadius, random), lakeRadius);
    int depthLimit = maxDepth.sample(random);
    int shelf = shelfWidth.sample(random);
    int slope = slopeWidth.sample(random);
    int size = 2 * lakeRadius + 1;
    BlockPos surface = origin.below();

    List<BlockPos> columns = new ArrayList<>();
    CrystalGrowth crystals = new CrystalGrowth(level);
    for (int x = 0; x < size; x++) {
      for (int z = 0; z < size; z++) {
        int distance = distances[x * size + z];
        if (distance == 0) {
          continue;
        }
        BlockPos column = surface.offset(x - lakeRadius, 0, z - lakeRadius);
        if (!CrystalGrowth.canAccess(level, column)) {
          return false;
        }
        columns.add(column);
        int depth = Math.min(depthLimit, 1 + Math.ceilDiv(Math.max(0, distance - shelf), slope));
        crystals.addColumn(column, Direction.DOWN, depth);
      }
    }
    long holes = columns.stream().filter(column -> level.getBlockState(column).isAir()).count();
    if (holes > columns.size() * MAX_HOLES) {
      return false;
    }
    return crystals.place(random, crystal, state -> CrystalGrowth.AIR_OR_WATER.test(state) || state.is(replaceable));
  }

  /**
   * The columns of a lake of {@code radius} as a {@code (2 * radius + 1)²} grid indexed by {@code x * size + z}, a few overlapping ellipses
   * of which the first is centered.
   */
  private static boolean[] shape(int radius, RandomSource random) {
    int size = 2 * radius + 1;
    boolean[] shape = new boolean[size * size];
    int ellipses = 3 + random.nextInt(3);
    for (int i = 0; i < ellipses; i++) {
      double centerX = i == 0 ? 0 : (random.nextDouble() - 0.5) * radius;
      double centerZ = i == 0 ? 0 : (random.nextDouble() - 0.5) * radius;
      double radiusX = (0.4 + random.nextDouble() * 0.6) * (radius - Math.abs(centerX)) + 0.5;
      double radiusZ = (0.4 + random.nextDouble() * 0.6) * (radius - Math.abs(centerZ)) + 0.5;
      for (int x = 0; x < size; x++) {
        for (int z = 0; z < size; z++) {
          double dx = (x - radius - centerX) / radiusX;
          double dz = (z - radius - centerZ) / radiusZ;
          if (dx * dx + dz * dz < 1) {
            shape[x * size + z] = true;
          }
        }
      }
    }
    return shape;
  }

  /**
   * The distance of each column of {@code shape} in steps to the nearest column outside the lake, 1 on its shore and 0 outside it.
   */
  private static int[] shoreDistances(boolean[] shape, int radius) {
    int size = 2 * radius + 1;
    int[] distances = new int[size * size];
    Arrays.fill(distances, Integer.MAX_VALUE);
    ArrayDeque<Integer> queue = new ArrayDeque<>();
    for (int x = 0; x < size; x++) {
      for (int z = 0; z < size; z++) {
        int index = x * size + z;
        if (!shape[index]) {
          distances[index] = 0;
          queue.add(index);
        } else if (x == 0 || z == 0 || x == size - 1 || z == size - 1) {
          // Next to the columns outside the grid.
          distances[index] = 1;
          queue.add(index);
        }
      }
    }
    while (!queue.isEmpty()) {
      int index = queue.poll();
      int x = index / size;
      int z = index % size;
      int[][] neighbours = {{x - 1, z}, {x + 1, z}, {x, z - 1}, {x, z + 1}};
      for (int[] neighbour : neighbours) {
        if (neighbour[0] < 0 || neighbour[1] < 0 || neighbour[0] >= size || neighbour[1] >= size) {
          continue;
        }
        int neighbourIndex = neighbour[0] * size + neighbour[1];
        if (distances[neighbourIndex] > distances[index] + 1) {
          distances[neighbourIndex] = distances[index] + 1;
          queue.add(neighbourIndex);
        }
      }
    }
    return distances;
  }
}
