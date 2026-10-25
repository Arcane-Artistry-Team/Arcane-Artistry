package gragongit.arcaneartistry.common.worldgen;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * A square tower of crystal growing straight out of a surface. From its origin in the air it searches up to {@code search_range} blocks
 * towards one of the surfaces in {@code attach_to} and grows a tower of one of the {@code sizes} back towards the origin. The columns of
 * the tower reach back to the surface where it is uneven, so that its base rests on it.
 */
public record CrystalPinFeature(Holder<BlockStateProvider> crystal, WeightedList<CrystalSize> sizes, List<CrystalAttachment> attachTo,
    int searchRange) implements Feature {
  public static final MapCodec<CrystalPinFeature> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(BlockStateProvider.CODEC.fieldOf("crystal").forGetter(CrystalPinFeature::crystal),
              WeightedList.codec(CrystalSize.CODEC).fieldOf("sizes").forGetter(CrystalPinFeature::sizes),
              ExtraCodecs.nonEmptyList(CrystalAttachment.CODEC.listOf()).fieldOf("attach_to").forGetter(CrystalPinFeature::attachTo),
              Codec.intRange(1, 16).fieldOf("search_range").forGetter(CrystalPinFeature::searchRange))
          .apply(instance, CrystalPinFeature::new));

  /**
   * How far the columns of a tower may reach back to an uneven surface.
   */
  private static final int MAX_ROOT = 3;

  @Override
  public MapCodec<CrystalPinFeature> codec() {
    return CODEC;
  }

  @Override
  public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
    if (!level.getBlockState(origin).isAir()) {
      return false;
    }
    Direction towardsSurface = Util.getRandom(attachTo, random).searchDirection(random);
    int distance = CrystalGrowth.distanceToSupport(level, origin, towardsSurface, searchRange);
    if (distance == 0) {
      return false;
    }
    CrystalSize size = sizes.getRandomOrThrow(random);
    BlockPos base = origin.relative(towardsSurface, distance - 1);
    Direction growth = towardsSurface.getOpposite();

    CrystalGrowth crystals = new CrystalGrowth(level);
    addTower(level, crystals, base, growth, size.thickness(), size.length().sample(random), random);
    return crystals.place(random, crystal, CrystalGrowth.AIR_OR_WATER);
  }

  /**
   * Adds a tower of {@code thickness}² columns with {@code length} cells each, growing in {@code growth} from the cell {@code base} next to
   * a surface. The square is shifted randomly so that it contains {@code base}, and each column reaches back to the surface.
   */
  static void addTower(WorldGenLevel level, CrystalGrowth crystals, BlockPos base, Direction growth, int thickness, int length,
      RandomSource random) {
    Direction[] sides = sides(growth);
    int offsetA = -random.nextInt(thickness);
    int offsetB = -random.nextInt(thickness);
    for (int a = 0; a < thickness; a++) {
      for (int b = 0; b < thickness; b++) {
        BlockPos start = base.relative(sides[0], offsetA + a).relative(sides[1], offsetB + b);
        int root = 0;
        while (root < MAX_ROOT && !CrystalGrowth.isSupport(level, start.relative(growth, -root - 1))) {
          root++;
        }
        if (root == MAX_ROOT) {
          root = 0;
        }
        // The base layer may meet bumps of the surface.
        for (int i = -root; i < length; i++) {
          if (i <= 0) {
            crystals.addOptional(start.relative(growth, i));
          } else {
            crystals.add(start.relative(growth, i));
          }
        }
      }
    }
  }

  /**
   * Two directions perpendicular to {@code direction} and to each other.
   */
  static Direction[] sides(Direction direction) {
    return switch (direction.getAxis()) {
      case X -> new Direction[] {Direction.UP, Direction.SOUTH};
      case Y -> new Direction[] {Direction.EAST, Direction.SOUTH};
      case Z -> new Direction[] {Direction.UP, Direction.EAST};
    };
  }
}
