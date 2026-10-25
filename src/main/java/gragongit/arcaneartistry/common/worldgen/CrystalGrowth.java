package gragongit.arcaneartistry.common.worldgen;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import gragongit.arcaneartistry.common.block.CrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * Collects the cells of a crystal structure and places them so that crystals neither float nor grow into anything:
 * <ul>
 * <li>Only cells connected through faces, not just edges or corners, to a full block are placed.</li>
 * <li>The structure is skipped if a required cell is blocked, that is if it cannot be replaced or touches a crystal placed before, even
 * diagonally. Optional cells, such as those of a base following an uneven surface, are left out instead.</li>
 * <li>The structure is skipped if it reaches beyond the area the feature may write to, so that it is not cut off.</li>
 * </ul>
 */
public final class CrystalGrowth {
  /**
   * Replaces air and water, the default for crystals growing into caves.
   */
  public static final Predicate<BlockState> AIR_OR_WATER = state -> state.isAir() || state.is(Blocks.WATER);

  private final WorldGenLevel level;
  private final Set<BlockPos> required = new LinkedHashSet<>();
  private final Set<BlockPos> optional = new LinkedHashSet<>();

  public CrystalGrowth(WorldGenLevel level) {
    this.level = level;
  }

  public void add(BlockPos pos) {
    required.add(pos.immutable());
  }

  /**
   * Adds a cell that is left out if it is blocked. Cells added both ways are optional.
   */
  public void addOptional(BlockPos pos) {
    optional.add(pos.immutable());
  }

  /**
   * Adds the cuboid from {@code min} to {@code max}, both inclusive.
   */
  public void addBox(BlockPos min, BlockPos max) {
    BlockPos.betweenClosed(min, max).forEach(this::add);
  }

  /**
   * Adds the cuboid from {@code min} to {@code max}, both inclusive, as optional cells.
   */
  public void addOptionalBox(BlockPos min, BlockPos max) {
    BlockPos.betweenClosed(min, max).forEach(this::addOptional);
  }

  /**
   * Adds a column of {@code length} cells starting at {@code start} and going in {@code direction}.
   */
  public void addColumn(BlockPos start, Direction direction, int length) {
    for (int i = 0; i < length; i++) {
      add(start.relative(direction, i));
    }
  }

  /**
   * Adds a column of up to {@code length} cells starting at {@code start} and going in {@code direction}, ending before the first cell
   * that is blocked for {@code canReplace}.
   */
  public void addColumnUntilBlocked(BlockPos start, Direction direction, int length, Predicate<BlockState> canReplace) {
    for (int i = 0; i < length; i++) {
      BlockPos pos = start.relative(direction, i);
      if (isBlocked(pos, canReplace)) {
        return;
      }
      addOptional(pos);
    }
  }

  /**
   * Places the collected cells as described in {@link CrystalGrowth}, replacing blocks that {@code canReplace} accepts.
   */
  public boolean place(RandomSource random, Holder<BlockStateProvider> crystal, Predicate<BlockState> canReplace) {
    if (!required.stream().allMatch(pos -> canAccess(level, pos)) || !optional.stream().allMatch(pos -> canAccess(level, pos))) {
      return false;
    }
    Set<BlockPos> placeable = new HashSet<>();
    for (BlockPos pos : required) {
      if (!optional.contains(pos)) {
        if (isBlocked(pos, canReplace)) {
          return false;
        }
        placeable.add(pos);
      }
    }
    for (BlockPos pos : optional) {
      if (!isBlocked(pos, canReplace)) {
        placeable.add(pos);
      }
    }

    Set<BlockPos> supported = new HashSet<>();
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    for (BlockPos pos : placeable) {
      for (Direction direction : Direction.values()) {
        BlockPos neighbour = pos.relative(direction);
        if (!placeable.contains(neighbour) && isSupport(level, neighbour)) {
          supported.add(pos);
          queue.add(pos);
          break;
        }
      }
    }
    while (!queue.isEmpty()) {
      BlockPos pos = queue.poll();
      for (Direction direction : Direction.values()) {
        BlockPos neighbour = pos.relative(direction);
        if (placeable.contains(neighbour) && supported.add(neighbour)) {
          queue.add(neighbour);
        }
      }
    }
    if (supported.isEmpty()) {
      return false;
    }

    for (BlockPos pos : supported) {
      level.setBlock(pos, crystal.value().getState(level, random, pos), Block.UPDATE_CLIENTS);
    }
    return true;
  }

  /**
   * Whether no crystal may be placed at {@code pos}, because {@code canReplace} rejects the block there or because it touches a crystal,
   * even diagonally.
   */
  private boolean isBlocked(BlockPos pos, Predicate<BlockState> canReplace) {
    if (!canAccess(level, pos) || !canReplace.test(level.getBlockState(pos))) {
      return true;
    }
    for (BlockPos neighbour : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
      if (canAccess(level, neighbour) && level.getBlockState(neighbour).getBlock() instanceof CrystalBlock) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether a crystal can rest against the block at {@code pos}, a full block.
   */
  public static boolean isSupport(WorldGenLevel level, BlockPos pos) {
    if (!canAccess(level, pos)) {
      return false;
    }
    BlockState state = level.getBlockState(pos);
    return !state.isAir() && !(state.getBlock() instanceof CrystalBlock) && state.isCollisionShapeFullBlock(level, pos);
  }

  /**
   * Whether the feature being generated may read and write the block at {@code pos}. Reading further away fails.
   */
  public static boolean canAccess(WorldGenLevel level, BlockPos pos) {
    return !(level instanceof WorldGenRegion region) || region.isWithinWriteZone(pos);
  }

  /**
   * The number of steps from {@code origin} in {@code direction}, at most {@code range}, until a {@linkplain #isSupport support} is
   * reached through air, or 0 if none is.
   */
  public static int distanceToSupport(WorldGenLevel level, BlockPos origin, Direction direction, int range) {
    BlockPos.MutableBlockPos pos = origin.mutable();
    for (int step = 1; step <= range; step++) {
      pos.move(direction);
      if (isSupport(level, pos)) {
        return step;
      }
      if (!canAccess(level, pos) || !level.getBlockState(pos).isAir()) {
        return 0;
      }
    }
    return 0;
  }
}
