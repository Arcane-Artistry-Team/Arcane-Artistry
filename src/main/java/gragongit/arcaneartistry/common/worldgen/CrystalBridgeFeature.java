package gragongit.arcaneartistry.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * A crystal connecting two opposite surfaces along one of the {@code axes}, such as a ceiling and a floor, {@code min_length} to
 * {@code max_length} blocks apart. It is one block thick, and one block thicker for each of the {@code thickness_lengths} that its length
 * reaches. Thicker bridges get thinner towards their middle by {@code pinch} of their extra thickness, but always
 * keep a column of one block from end to end. At each end, {@code base_towers} single columns of {@code base_tower_length} stick to the
 * bridge so that its base looks thicker.
 */
public record CrystalBridgeFeature(Holder<BlockStateProvider> crystal, List<Direction.Axis> axes, int minLength, int maxLength,
    List<Integer> thicknessLengths, FloatProvider pinch, IntProvider baseTowers, IntProvider baseTowerLength) implements Feature {
  public static final MapCodec<CrystalBridgeFeature> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(BlockStateProvider.CODEC.fieldOf("crystal").forGetter(CrystalBridgeFeature::crystal),
              ExtraCodecs.nonEmptyList(Direction.Axis.CODEC.listOf()).fieldOf("axes").forGetter(CrystalBridgeFeature::axes),
              Codec.intRange(1, 32).fieldOf("min_length").forGetter(CrystalBridgeFeature::minLength),
              Codec.intRange(1, 32).fieldOf("max_length").forGetter(CrystalBridgeFeature::maxLength),
              Codec.intRange(1, 32).listOf(0, 7).fieldOf("thickness_lengths").forGetter(CrystalBridgeFeature::thicknessLengths),
              FloatProviders.codec(0F, 1F).fieldOf("pinch").forGetter(CrystalBridgeFeature::pinch),
              IntProviders.codec(0, 16).fieldOf("base_towers").forGetter(CrystalBridgeFeature::baseTowers),
              IntProviders.codec(1, 8).fieldOf("base_tower_length").forGetter(CrystalBridgeFeature::baseTowerLength))
          .apply(instance, CrystalBridgeFeature::new));

  @Override
  public MapCodec<CrystalBridgeFeature> codec() {
    return CODEC;
  }

  @Override
  public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
    if (!level.getBlockState(origin).isAir()) {
      return false;
    }
    Direction.Axis axis = Util.getRandom(axes, random);
    Direction up = Direction.get(Direction.AxisDirection.POSITIVE, axis);
    Direction down = up.getOpposite();
    int distanceUp = CrystalGrowth.distanceToSupport(level, origin, up, maxLength);
    int distanceDown = CrystalGrowth.distanceToSupport(level, origin, down, maxLength);
    int length = distanceUp + distanceDown - 1;
    if (distanceUp == 0 || distanceDown == 0 || length < minLength || length > maxLength) {
      return false;
    }
    int thickness = 1 + (int) thicknessLengths.stream().filter(thicknessLength -> length >= thicknessLength).count();
    BlockPos low = origin.relative(down, distanceDown - 1);
    BlockPos high = origin.relative(up, distanceUp - 1);
    float pinchAmount = pinch.sample(random);
    Direction[] sides = CrystalPinFeature.sides(up);
    List<Column> columns = crossSection(thickness, random);

    CrystalGrowth crystals = new CrystalGrowth(level);
    for (Column column : columns) {
      BlockPos middle = origin.relative(sides[0], column.a).relative(sides[1], column.b);
      if (!CrystalGrowth.canAccess(level, middle) || !level.getBlockState(middle).isAir()) {
        continue;
      }
      // The columns besides the core follow the surfaces, which may be uneven. Columns that do not reach both are left out.
      int columnUp = CrystalGrowth.distanceToSupport(level, middle, up, maxLength);
      int columnDown = CrystalGrowth.distanceToSupport(level, middle, down, maxLength);
      if (columnUp == 0 || columnDown == 0) {
        continue;
      }
      BlockPos start = middle.relative(down, columnDown - 1);
      BlockPos end = middle.relative(up, columnUp - 1);
      for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
        double progress = length == 1 ? 0 : Mth.clamp((double) (pos.get(axis) - low.get(axis)) / (length - 1), 0, 1);
        double middleness = 1 - Math.abs(2 * progress - 1);
        int localThickness = thickness - (int) Math.round(pinchAmount * (thickness - 1) * middleness);
        if (column.thickness <= localThickness) {
          crystals.add(pos);
        }
      }
    }

    for (BlockPos end : List.of(low, high)) {
      Direction towardsSurface = end == low ? down : up;
      List<Column> ring = ring(columns, thickness);
      Util.shuffle(ring, random);
      int towers = Math.min(baseTowers.sample(random), ring.size());
      for (int i = 0; i < towers; i++) {
        BlockPos start = end.relative(sides[0], ring.get(i).a).relative(sides[1], ring.get(i).b);
        if (!CrystalGrowth.canAccess(level, start) || !level.getBlockState(start).isAir()) {
          continue;
        }
        int distance = CrystalGrowth.distanceToSupport(level, start, towardsSurface, 3);
        if (distance > 0) {
          crystals
              .addColumnUntilBlocked(start.relative(towardsSurface, distance - 1), towardsSurface.getOpposite(),
                  baseTowerLength.sample(random), CrystalGrowth.AIR_OR_WATER);
        }
      }
    }
    return crystals.place(random, crystal, CrystalGrowth.AIR_OR_WATER);
  }

  /**
   * The columns of a cross section {@code thickness} blocks wide around the core column at (0, 0), each with the thickness of the bridge
   * from which it is part of it. The squares of each thickness are nested, each growing from the last towards a random corner, so the
   * bridge stays connected where it narrows.
   */
  private static List<Column> crossSection(int thickness, RandomSource random) {
    List<Column> columns = new ArrayList<>(List.of(new Column(0, 0, 1)));
    int minA = 0;
    int minB = 0;
    for (int size = 2; size <= thickness; size++) {
      minA -= random.nextInt(2);
      minB -= random.nextInt(2);
      for (int a = minA; a < minA + size; a++) {
        for (int b = minB; b < minB + size; b++) {
          if (!contains(columns, a, b)) {
            columns.add(new Column(a, b, size));
          }
        }
      }
    }
    return columns;
  }

  private static boolean contains(List<Column> columns, int a, int b) {
    return columns.stream().anyMatch(column -> column.a == a && column.b == b);
  }

  /**
   * The columns next to the cross section, including diagonally, where the base towers may grow.
   */
  private static List<Column> ring(List<Column> columns, int thickness) {
    List<Column> ring = new ArrayList<>();
    for (int a = -thickness - 1; a <= thickness + 1; a++) {
      for (int b = -thickness - 1; b <= thickness + 1; b++) {
        int columnA = a;
        int columnB = b;
        boolean adjacent = columns.stream().anyMatch(column -> Math.abs(column.a - columnA) <= 1 && Math.abs(column.b - columnB) <= 1);
        if (!contains(columns, a, b) && adjacent) {
          ring.add(new Column(a, b, thickness));
        }
      }
    }
    return ring;
  }

  /**
   * A column of the cross section at offset ({@code a}, {@code b}) from the core, part of bridges at least {@code thickness} thick.
   */
  private record Column(int a, int b, int thickness) {
  }
}
