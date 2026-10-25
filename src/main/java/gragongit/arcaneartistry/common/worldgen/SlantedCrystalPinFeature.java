package gragongit.arcaneartistry.common.worldgen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * A crystal growing at a slant out of the surfaces of a cave, as cubes of one of the {@code sizes}, each one block further
 * along the slant than the last. An {@link Slant#EDGE edge} slant moves along two axes per step and grows best out of an edge between two
 * surfaces, a {@link Slant#CORNER corner} slant moves along all three and grows best out of a corner. From its origin in the air the
 * feature looks for surfaces up to {@code search_range} blocks away and grows away from the nearest ones, picking a random slant for axes
 * without a surface. Around the base, {@code base_towers} single columns of {@code base_tower_length} grow straight out of the surfaces.
 */
public record SlantedCrystalPinFeature(Holder<BlockStateProvider> crystal, Slant slant, WeightedList<CrystalSize> sizes,
    IntProvider baseTowers, IntProvider baseTowerLength, int searchRange) implements Feature {
  public static final MapCodec<SlantedCrystalPinFeature> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(BlockStateProvider.CODEC.fieldOf("crystal").forGetter(SlantedCrystalPinFeature::crystal),
              Slant.CODEC.fieldOf("slant").forGetter(SlantedCrystalPinFeature::slant),
              WeightedList.codec(CrystalSize.CODEC).fieldOf("sizes").forGetter(SlantedCrystalPinFeature::sizes),
              IntProviders.codec(0, 16).fieldOf("base_towers").forGetter(SlantedCrystalPinFeature::baseTowers),
              IntProviders.codec(1, 8).fieldOf("base_tower_length").forGetter(SlantedCrystalPinFeature::baseTowerLength),
              Codec.intRange(1, 16).fieldOf("search_range").forGetter(SlantedCrystalPinFeature::searchRange))
          .apply(instance, SlantedCrystalPinFeature::new));

  @Override
  public MapCodec<SlantedCrystalPinFeature> codec() {
    return CODEC;
  }

  @Override
  public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
    if (!level.getBlockState(origin).isAir()) {
      return false;
    }

    // The nearest surface on each axis, nearest first.
    List<Direction> surfaces = new ArrayList<>();
    int[] distances = new int[Direction.values().length];
    for (Direction.Axis axis : Direction.Axis.values()) {
      Direction nearest = null;
      for (Direction direction : List.of(Direction.get(Direction.AxisDirection.POSITIVE, axis),
          Direction.get(Direction.AxisDirection.NEGATIVE, axis))) {
        int distance = CrystalGrowth.distanceToSupport(level, origin, direction, searchRange);
        distances[direction.ordinal()] = distance;
        if (distance > 0 && (nearest == null || distance < distances[nearest.ordinal()])) {
          nearest = direction;
        }
      }
      if (nearest != null) {
        surfaces.add(nearest);
      }
    }
    if (surfaces.isEmpty()) {
      return false;
    }
    surfaces.sort(Comparator.comparingInt(direction -> distances[direction.ordinal()]));
    surfaces = new ArrayList<>(surfaces.subList(0, Math.min(surfaces.size(), slant.axes)));

    // Move next to the surfaces, measuring again since they may be uneven.
    BlockPos base = origin;
    for (int pass = 0; pass < 2; pass++) {
      for (Direction surface : surfaces) {
        int distance = CrystalGrowth.distanceToSupport(level, base, surface, searchRange);
        if (distance == 0) {
          return false;
        }
        base = base.relative(surface, distance - 1);
      }
    }

    // Grow away from the surfaces, and at a random slant on further axes.
    List<Direction> growth = new ArrayList<>(surfaces.stream().map(Direction::getOpposite).toList());
    List<Direction.Axis> freeAxes = new ArrayList<>(List.of(Direction.Axis.values()));
    surfaces.forEach(surface -> freeAxes.remove(surface.getAxis()));
    Util.shuffle(freeAxes, random);
    for (int i = growth.size(); i < slant.axes; i++) {
      Direction.Axis axis = freeAxes.removeFirst();
      growth.add(Direction.get(random.nextBoolean() ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE, axis));
    }

    CrystalSize crystalSize = sizes.getRandomOrThrow(random);
    int size = crystalSize.thickness();
    // The first cube extends away from the surfaces along the slant and is shifted randomly along the remaining axis, if any.
    BlockPos corner = base;
    Vec3i step = Vec3i.ZERO;
    for (Direction direction : growth) {
      step = step.offset(direction.getUnitVec3i());
      if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
        corner = corner.relative(direction, size - 1);
      }
    }
    for (Direction.Axis axis : freeAxes) {
      corner = corner.relative(Direction.get(Direction.AxisDirection.NEGATIVE, axis), random.nextInt(size));
    }

    CrystalGrowth crystals = new CrystalGrowth(level);
    int cubes = crystalSize.length().sample(random);
    for (int i = 0; i < cubes; i++) {
      BlockPos min = corner.offset(step.multiply(i));
      // The first cube is the base, which may meet bumps of the surfaces.
      if (i == 0) {
        crystals.addOptionalBox(min, min.offset(size - 1, size - 1, size - 1));
      } else {
        crystals.addBox(min, min.offset(size - 1, size - 1, size - 1));
      }
    }

    int towers = baseTowers.sample(random);
    for (int i = 0; i < towers; i++) {
      Direction surface = Util.getRandom(surfaces, random);
      BlockPos start = base;
      for (Direction direction : growth) {
        if (direction.getAxis() != surface.getAxis()) {
          start = start.relative(direction, random.nextInt(size + 3) - 1);
        }
      }
      if (!CrystalGrowth.canAccess(level, start) || !level.getBlockState(start).isAir()) {
        continue;
      }
      int distance = CrystalGrowth.distanceToSupport(level, start, surface, 3);
      if (distance > 0) {
        crystals
            .addColumnUntilBlocked(start.relative(surface, distance - 1), surface.getOpposite(), baseTowerLength.sample(random),
                CrystalGrowth.AIR_OR_WATER);
      }
    }
    return crystals.place(random, crystal, CrystalGrowth.AIR_OR_WATER);
  }

  public enum Slant implements StringRepresentable {
    EDGE("edge", 2), CORNER("corner", 3);

    public static final Codec<Slant> CODEC = StringRepresentable.fromEnum(Slant::values);

    private final String name;
    /**
     * The number of axes along which the crystal moves per step.
     */
    private final int axes;

    Slant(String name, int axes) {
      this.name = name;
      this.axes = axes;
    }

    @Override
    public String getSerializedName() {
      return name;
    }
  }
}
