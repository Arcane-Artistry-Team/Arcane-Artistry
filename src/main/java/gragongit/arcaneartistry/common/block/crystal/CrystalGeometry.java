package gragongit.arcaneartistry.common.block.crystal;

import static gragongit.arcaneartistry.common.block.CrystalBlock.CRYSTAL;
import static gragongit.arcaneartistry.common.block.CrystalBlock.EMPTY;
import static gragongit.arcaneartistry.common.block.CrystalBlock.INSET;
import static gragongit.arcaneartistry.common.block.CrystalBlock.SOLID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import gragongit.arcaneartistry.common.block.CrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

/**
 * Builds the surface of a connected crystal inside a single cell. All coordinates are in pixels relative to that cell, so the cell spans
 * {@code [0, 16]^3}. Geometry is only ever produced inside crystal blocks.
 *
 * <p>The crystal is modelled as a union of convex solids:
 * <ul>
 * <li>Each crystal block has a core box. It is inset by {@link CrystalBlock#INSET} pixels towards empty neighbours and reaches the block
 * boundary towards crystals and solid blocks.</li>
 * <li>A crystal that is a step of a diagonal staircase is cut in half along its diagonal (see {@link #cutMask}). The cut runs through the
 * block's corners and the cut-off sides are not inset, so the cuts of a staircase line up to one smooth slope.</li>
 * <li>Two crystals sharing only an edge, where exactly one of the two cells next to the edge is a crystal, get a small 45 degree wedge
 * through the shared edge that smooths the inner corner.</li>
 * </ul>
 *
 * <p>The geometry of a cell depends on the 3x3x3 cells around it and on how the cell and its direct neighbours are cut. Both are stored in
 * a {@link Key}.
 */
public final class CrystalGeometry {
  public static final int SIZE = 16;

  private static final double MARGIN_SAMPLE_DEPTH = 0.5;
  private static final int[][] PLANES = {{0, 1}, {0, 2}, {1, 2}};
  private static final int PLANE_CUT_BITS = 3;
  private static final int CELL_CUT_BITS = PLANES.length * PLANE_CUT_BITS;

  private final Key key;
  private final List<ConvexSolid> solids = new ArrayList<>();

  private CrystalGeometry(Key key) {
    this.key = key;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          if (get(x, y, z) == CRYSTAL) {
            int[] cell = {x, y, z};
            add(core(cell));
            addWedges(cell);
          }
        }
      }
    }
  }

  /**
   * The classifications of the cells around a crystal, relative to it.
   */
  @FunctionalInterface
  public interface Cells {
    int get(int x, int y, int z);
  }

  /**
   * Lazily classifies the blocks around a position in a level. Each block is looked up at most once.
   */
  public static final class LevelCells implements Cells {
    private static final int RADIUS = 3;
    private static final int WIDTH = RADIUS * 2 + 1;

    private final BlockGetter level;
    private final BlockPos origin;
    private final Block crystal;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private final byte[] classifications = new byte[WIDTH * WIDTH * WIDTH];

    public LevelCells(BlockGetter level, BlockPos origin, Block crystal) {
      this.level = level;
      this.origin = origin;
      this.crystal = crystal;
      Arrays.fill(classifications, (byte) -1);
    }

    @Override
    public int get(int x, int y, int z) {
      if (Math.abs(x) > RADIUS || Math.abs(y) > RADIUS || Math.abs(z) > RADIUS) {
        return CrystalBlock.classify(level, cursor.setWithOffset(origin, x, y, z), crystal);
      }
      int index = ((x + RADIUS) * WIDTH + y + RADIUS) * WIDTH + z + RADIUS;
      if (classifications[index] < 0) {
        classifications[index] = (byte) CrystalBlock.classify(level, cursor.setWithOffset(origin, x, y, z), crystal);
      }
      return classifications[index];
    }
  }

  /**
   * The surface of the crystal inside the cell.
   */
  public record Result(List<Polygon> surface, long[][] rubbleMargins) {}

  /**
   * Everything the geometry of a crystal depends on: the classifications of the 3x3x3 cells around it, two bits per cell, and the
   * {@linkplain #cutMask cuts} of the crystal and its six direct neighbours.
   */
  public record Key(long cells, long cuts) {
    private static int index(int x, int y, int z) {
      return (x + 1) * 9 + (y + 1) * 3 + (z + 1);
    }

    int get(int x, int y, int z) {
      if (Math.abs(x) > 1 || Math.abs(y) > 1 || Math.abs(z) > 1) {
        return EMPTY;
      }
      return (int) (cells >>> (index(x, y, z) * 2) & 3);
    }

    /**
     * The cuts of the cell at the given offset. Only known for the center and its direct neighbours, other cells are not cut.
     */
    int cutMask(int x, int y, int z) {
      int slot = cutSlot(x, y, z);
      return slot < 0 ? 0 : (int) (cuts >>> (slot * CELL_CUT_BITS) & ((1 << CELL_CUT_BITS) - 1));
    }

    private static int cutSlot(int x, int y, int z) {
      if (x == 0 && y == 0 && z == 0) {
        return 0;
      }
      for (Direction direction : Direction.values()) {
        if (x == direction.getStepX() && y == direction.getStepY() && z == direction.getStepZ()) {
          return direction.ordinal() + 1;
        }
      }
      return -1;
    }
  }

  /**
   * Creates the key of the crystal at the center of {@code cells}. The cuts of the neighbours are only needed for rendering, the shape of
   * the crystal itself only depends on its own cuts.
   */
  public static Key key(Cells cells, boolean withNeighborCuts) {
    long classifications = 0;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          classifications |= (long) cells.get(x, y, z) << (Key.index(x, y, z) * 2);
        }
      }
    }

    long cuts = 0;
    if (cells.get(0, 0, 0) == CRYSTAL) {
      cuts |= cutMask(cells, new int[3]);
    }
    if (withNeighborCuts) {
      for (Direction direction : Direction.values()) {
        int[] neighbor = {direction.getStepX(), direction.getStepY(), direction.getStepZ()};
        if (cells.get(neighbor[0], neighbor[1], neighbor[2]) == CRYSTAL) {
          cuts |= (long) cutMask(cells, neighbor) << (Key.cutSlot(neighbor[0], neighbor[1], neighbor[2]) * CELL_CUT_BITS);
        }
      }
    }
    return new Key(classifications, cuts);
  }

  /**
   * The key of the crystal at {@code pos} without the cuts of its neighbours, which is enough to know its own shape.
   */
  public static Key shapeKey(BlockGetter level, BlockPos pos, Block crystal) {
    return key(new LevelCells(level, pos, crystal), false);
  }

  /**
   * Computes the diagonal cuts of the crystal at {@code cell}. A cell can be cut in a plane if it is a step there: it has crystals on two
   * perpendicular sides and no crystals on the two opposite sides (air or solid blocks). The step is cut if the slope continues along the
   * diagonal with another crystal, either the diagonal neighbour in the same plane or, for staircases along the space diagonal, the
   * diagonal neighbour shifted by one block along the third axis being the same kind of step. The neighbours along the third axis must not
   * be crystals that are not the same kind of step, since the slope would run into their sides. So the slope leads from crystal to crystal
   * instead of into the air, and corners of cubes and L shapes stay square.
   *
   * <p>The result holds {@value #PLANE_CUT_BITS} bits per plane of {@link #PLANES}: 0 if not cut, otherwise {@code 1 + a + 2b}, where
   * {@code a} and {@code b} are 1 if the kept half lies towards the positive side of the plane's first and second axis.
   */
  static int cutMask(Cells cells, int[] cell) {
    int mask = 0;
    for (int plane = 0; plane < PLANES.length; plane++) {
      int axisA = PLANES[plane][0];
      int axisB = PLANES[plane][1];
      for (int signA = -1; signA <= 1; signA += 2) {
        for (int signB = -1; signB <= 1; signB += 2) {
          if (isStep(cells, cell, axisA, signA, axisB, signB) && !runsIntoCrystal(cells, cell, axisA, signA, axisB, signB)
              && (continuesSlope(cells, cell, axisA, signA, axisB, signB, 1)
                  || continuesSlope(cells, cell, axisA, signA, axisB, signB, -1))) {
            mask |= (1 + (signA > 0 ? 1 : 0) + (signB > 0 ? 2 : 0)) << (plane * PLANE_CUT_BITS);
          }
        }
      }
    }
    return mask;
  }

  private static boolean isStep(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    return get(cells, cell, axisA, signA, axisB, 0, 0) == CRYSTAL && get(cells, cell, axisA, 0, axisB, signB, 0) == CRYSTAL
        && get(cells, cell, axisA, -signA, axisB, 0, 0) != CRYSTAL && get(cells, cell, axisA, 0, axisB, -signB, 0) != CRYSTAL;
  }

  /**
   * Whether a neighbour along the third axis is a crystal that is not the same kind of step. The slope would run into its side.
   */
  private static boolean runsIntoCrystal(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    for (int shift = -1; shift <= 1; shift += 2) {
      int[] neighbor = offset(cell, axisA, 0, axisB, 0, shift);
      if (cells.get(neighbor[0], neighbor[1], neighbor[2]) == CRYSTAL && !isStep(cells, neighbor, axisA, signA, axisB, signB)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether the slope of a step continues in the given direction along its diagonal.
   */
  private static boolean continuesSlope(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB, int direction) {
    int stepA = signA * direction;
    int stepB = -signB * direction;
    if (get(cells, cell, axisA, stepA, axisB, stepB, 0) == CRYSTAL) {
      return true;
    }
    for (int shift = -1; shift <= 1; shift += 2) {
      int[] neighbor = offset(cell, axisA, stepA, axisB, stepB, shift);
      if (cells.get(neighbor[0], neighbor[1], neighbor[2]) == CRYSTAL && isStep(cells, neighbor, axisA, signA, axisB, signB)) {
        return true;
      }
    }
    return false;
  }

  private static int get(Cells cells, int[] cell, int axisA, int stepA, int axisB, int stepB, int shift) {
    int[] neighbor = offset(cell, axisA, stepA, axisB, stepB, shift);
    return cells.get(neighbor[0], neighbor[1], neighbor[2]);
  }

  private static int[] offset(int[] cell, int axisA, int stepA, int axisB, int stepB, int shift) {
    int[] neighbor = cell.clone();
    neighbor[axisA] += stepA;
    neighbor[axisB] += stepB;
    neighbor[3 - axisA - axisB] += shift;
    return neighbor;
  }

  public static Result build(Key key) {
    return new CrystalGeometry(key).build();
  }

  static CrystalGeometry of(Key key) {
    return new CrystalGeometry(key);
  }

  /**
   * Whether the point lies inside the crystal.
   */
  boolean contains(Vector3dc point) {
    for (ConvexSolid solid : solids) {
      if (solid.contains(point, -Polygon.EPSILON)) {
        return true;
      }
    }
    return false;
  }

  private Result build() {
    List<Polygon> surface = ConvexSolid.unionSurface(solids, 0, SIZE);
    long[][] margins = new long[6][];
    if (get(0, 0, 0) == CRYSTAL) {
      for (Direction direction : Direction.values()) {
        if (get(direction.getStepX(), direction.getStepY(), direction.getStepZ()) == SOLID) {
          margins[direction.ordinal()] = rubbleMargin(direction);
        }
      }
    }
    return new Result(surface, margins);
  }

  private int get(int x, int y, int z) {
    return key.get(x, y, z);
  }

  private int get(int[] cell) {
    return get(cell[0], cell[1], cell[2]);
  }

  private int get(int[] cell, int axis, int step) {
    int[] neighbor = cell.clone();
    neighbor[axis] += step;
    return get(neighbor);
  }

  private void add(ConvexSolid solid) {
    if (!solid.isEmpty() && solid.intersectsBox(0, SIZE)) {
      solids.add(solid);
    }
  }

  /**
   * The cuts of a cell as {@code [axisA, axisB, signA, signB]}, where the signs point to the two neighbouring crystals whose half of the
   * cell is kept.
   */
  private List<int[]> cuts(int[] cell) {
    int mask = key.cutMask(cell[0], cell[1], cell[2]);
    List<int[]> cuts = new ArrayList<>(3);
    for (int plane = 0; plane < PLANES.length; plane++) {
      int value = mask >>> (plane * PLANE_CUT_BITS) & ((1 << PLANE_CUT_BITS) - 1);
      if (value != 0) {
        int signs = value - 1;
        cuts.add(new int[] {PLANES[plane][0], PLANES[plane][1], (signs & 1) != 0 ? 1 : -1, (signs & 2) != 0 ? 1 : -1});
      }
    }
    return cuts;
  }

  private ConvexSolid core(int[] cell) {
    List<int[]> cuts = cuts(cell);
    double[] min = new double[3];
    double[] max = new double[3];
    for (int axis = 0; axis < 3; axis++) {
      min[axis] = cell[axis] * SIZE + inset(cell, cuts, axis, -1);
      max[axis] = cell[axis] * SIZE + SIZE - inset(cell, cuts, axis, 1);
    }

    ConvexSolid core = ConvexSolid.box(min, max);
    Vector3d center = new Vector3d(cell[0], cell[1], cell[2]).mul(SIZE).add(SIZE / 2D, SIZE / 2D, SIZE / 2D);
    for (int[] cut : cuts) {
      Vector3d normal = new Vector3d().setComponent(cut[0], -cut[2]).setComponent(cut[1], -cut[3]);
      core = core.cut(normal, center);
    }
    return core;
  }

  /**
   * The inset of a cell's core on one side. Sides cut off by a diagonal cut are not inset, so that the cuts of neighbouring cells meet
   * in the block corners.
   */
  private double inset(int[] cell, List<int[]> cuts, int axis, int step) {
    if (get(cell, axis, step) != EMPTY) {
      return 0;
    }
    for (int[] cut : cuts) {
      if (cut[0] == axis && cut[2] == -step || cut[1] == axis && cut[3] == -step) {
        return 0;
      }
    }
    return INSET;
  }

  private double plainMin(int[] cell, int axis) {
    return cell[axis] * SIZE + (get(cell, axis, -1) != EMPTY ? 0 : INSET);
  }

  private double plainMax(int[] cell, int axis) {
    return cell[axis] * SIZE + SIZE - (get(cell, axis, 1) != EMPTY ? 0 : INSET);
  }

  /**
   * Adds the wedges from {@code first} to all crystals sharing an edge with it that come after it, so that every pair is handled once.
   */
  private void addWedges(int[] first) {
    for (int x = first[0]; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          int[] second = {x, y, z};
          if (Key.index(x, y, z) <= Key.index(first[0], first[1], first[2]) || get(second) != CRYSTAL) {
            continue;
          }

          int changedAxes = 0;
          boolean adjacent = true;
          for (int axis = 0; axis < 3; axis++) {
            int difference = Math.abs(second[axis] - first[axis]);
            adjacent &= difference <= 1;
            changedAxes += difference;
          }
          if (adjacent && changedAxes == 2) {
            addWedge(first, second);
          }
        }
      }
    }
  }

  /**
   * Adds the 45 degree wedge between two crystals sharing an edge if exactly one of the two cells next to the edge is a crystal. The
   * wedge's slanted face passes through the shared edge and faces the other cell.
   */
  private void addWedge(int[] first, int[] second) {
    int edgeAxis = 0;
    while (first[edgeAxis] != second[edgeAxis]) {
      edgeAxis++;
    }
    int axisA = (edgeAxis + 1) % 3;
    int axisB = (edgeAxis + 2) % 3;

    int[] sideA = first.clone();
    sideA[axisA] = second[axisA];
    int[] sideB = first.clone();
    sideB[axisB] = second[axisB];
    boolean crystalA = get(sideA) == CRYSTAL;
    boolean crystalB = get(sideB) == CRYSTAL;
    if (crystalA == crystalB) {
      return;
    }

    // The plain insets only depend on cells both crystals know, so both compute the same wedge.
    double edgeMin = Math.max(plainMin(first, edgeAxis), plainMin(second, edgeAxis));
    double edgeMax = Math.min(plainMax(first, edgeAxis), plainMax(second, edgeAxis));
    if (edgeMax <= edgeMin) {
      return;
    }

    int[] emptySide = crystalA ? sideB : sideA;
    double edgeA = Math.max(first[axisA], second[axisA]) * SIZE;
    double edgeB = Math.max(first[axisB], second[axisB]) * SIZE;
    double signA = Math.signum(emptySide[axisA] * SIZE + SIZE / 2D - edgeA);
    double signB = Math.signum(emptySide[axisB] * SIZE + SIZE / 2D - edgeB);

    double[][] offsets = {{-INSET, -INSET}, {-INSET, INSET}, {INSET, -INSET}};
    List<Vector3d> points = new ArrayList<>(6);
    for (double[] offset : offsets) {
      for (double edge : new double[] {edgeMin, edgeMax}) {
        Vector3d point = new Vector3d();
        point.setComponent(axisA, edgeA + offset[0] * signA);
        point.setComponent(axisB, edgeB + offset[1] * signB);
        point.setComponent(edgeAxis, edge);
        points.add(point);
      }
    }
    add(ConvexSolid.hull(points));
  }

  /**
   * Computes which pixels of the face towards {@code direction} are not covered by the crystal. Bit {@code v * 16 + u} is set for an
   * uncovered pixel, where {@code u} and {@code v} run along the two other axes in x, y, z order.
   */
  private long[] rubbleMargin(Direction direction) {
    int axis = direction.getAxis().ordinal();
    int axisU = axis == 0 ? 1 : 0;
    int axisV = axis == 2 ? 1 : 2;
    double depth = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? SIZE - MARGIN_SAMPLE_DEPTH : MARGIN_SAMPLE_DEPTH;

    long[] margin = new long[SIZE * SIZE / 64];
    Vector3d point = new Vector3d();
    for (int v = 0; v < SIZE; v++) {
      for (int u = 0; u < SIZE; u++) {
        point.setComponent(axis, depth).setComponent(axisU, u + 0.5).setComponent(axisV, v + 0.5);
        if (solids.stream().noneMatch(solid -> solid.contains(point, Polygon.EPSILON))) {
          int bit = v * SIZE + u;
          margin[bit >> 6] |= 1L << (bit & 63);
        }
      }
    }
    return margin;
  }
}
