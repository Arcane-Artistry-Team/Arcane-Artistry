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
 * <li>Every edge of a crystal whose two adjacent sides are open to the air is cut off (see {@link #cutMask}). Steps of a staircase are
 * cut through the diagonal of their inset box, so the cuts of a staircase line up to one smooth slope. Other edges are cut through the
 * centers of the adjacent sides, which turns the ends of crystals into tips. Sides touching solid blocks are never cut, so crystals look
 * like they grow out of them.</li>
 * <li>Inner corners, where a crystal has crystals on both sides of an edge but air diagonally behind it, are cut off by 45 degrees
 * along the inset sides of the neighbours, so the inset walls and slopes of the neighbours run past the corner without a tooth.</li>
 * </ul>
 *
 * <p>The geometry of a cell depends on the 3x3x3 cells around it and on how the cell and its direct neighbours are cut. Both are stored in
 * a {@link Key}.
 */
public final class CrystalGeometry {
  public static final int SIZE = 16;

  private static final double MARGIN_SAMPLE_DEPTH = 0.5;
  private static final int[][] PLANES = {{0, 1}, {0, 2}, {1, 2}};
  private static final int EDGES_PER_PLANE = 4;
  private static final int EDGE_BITS = 2;
  private static final int CELL_CUT_BITS = PLANES.length * EDGES_PER_PLANE * EDGE_BITS;
  private static final int CUT_SLOTS_PER_WORD = Long.SIZE / CELL_CUT_BITS;

  /** The edge is not cut. */
  private static final int NO_CUT = 0;
  /** The edge is cut off as the tip of a crystal, see {@link #cutMask}. */
  private static final int TIP_CUT = 1;
  /** The edge is cut along the slope of a staircase step. */
  private static final int STEP_CUT = 2;
  /** The edge is an inner corner and cut off along the inset sides of the neighbours. */
  private static final int INNER_CUT = 3;
  /**
   * How far along an edge other crystals are checked. Together with the direct neighbours whose cuts are part of the {@link Key}, this
   * stays within the radius of {@link LevelCells}.
   */
  private static final int MAX_EDGE_DISTANCE = 2;

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
  public record Key(long cells, long cuts0, long cuts1, long cuts2, long cuts3) {
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
      if (slot < 0) {
        return 0;
      }
      long word = switch (slot / CUT_SLOTS_PER_WORD) {
        case 0 -> cuts0;
        case 1 -> cuts1;
        case 2 -> cuts2;
        default -> cuts3;
      };
      return (int) (word >>> (slot % CUT_SLOTS_PER_WORD * CELL_CUT_BITS) & ((1 << CELL_CUT_BITS) - 1));
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

    long[] cuts = new long[4];
    if (cells.get(0, 0, 0) == CRYSTAL) {
      cuts[0] |= cutMask(cells, new int[3]);
    }
    if (withNeighborCuts) {
      for (Direction direction : Direction.values()) {
        int[] neighbor = {direction.getStepX(), direction.getStepY(), direction.getStepZ()};
        if (cells.get(neighbor[0], neighbor[1], neighbor[2]) == CRYSTAL) {
          int slot = Key.cutSlot(neighbor[0], neighbor[1], neighbor[2]);
          cuts[slot / CUT_SLOTS_PER_WORD] |= (long) cutMask(cells, neighbor) << (slot % CUT_SLOTS_PER_WORD * CELL_CUT_BITS);
        }
      }
    }
    return new Key(classifications, cuts[0], cuts[1], cuts[2], cuts[3]);
  }

  /**
   * The key of the crystal at {@code pos} without the cuts of its neighbours, which is enough to know its own shape.
   */
  public static Key shapeKey(BlockGetter level, BlockPos pos, Block crystal) {
    return key(new LevelCells(level, pos, crystal), false);
  }

  /**
   * Computes how the edges of the crystal at {@code cell} are cut. An edge whose two sides are open to the air is cut off: along the
   * slope if the crystal is a step of a staircase, i.e. the opposite edge has crystals on both sides, and as a tip otherwise. Two
   * exceptions keep neighbouring cuts consistent:
   * <ul>
   * <li>Other edges of a step sharing an open side with it are not cut. The slope of the step already shapes these sides.</li>
   * <li>If the crystals continuing along the edge do not cut it, the edge is not cut either, so the cut does not run into their side.</li>
   * </ul>
   * An inner corner, with crystals on both sides and air diagonally behind it, is cut off along the inset sides of the neighbours.
   *
   * <p>The result holds {@value #EDGE_BITS} bits per edge, {@value #EDGES_PER_PLANE} edges per plane of {@link #PLANES}: edge
   * {@code 4 * plane + a + 2b} is the edge on the positive side of the plane's first axis if {@code a} is 1 and on the positive side of its
   * second axis if {@code b} is 1.
   */
  static int cutMask(Cells cells, int[] cell) {
    int mask = 0;
    for (int plane = 0; plane < PLANES.length; plane++) {
      int axisA = PLANES[plane][0];
      int axisB = PLANES[plane][1];
      for (int signA = -1; signA <= 1; signA += 2) {
        for (int signB = -1; signB <= 1; signB += 2) {
          int type = NO_CUT;
          if (wouldCut(cells, cell, axisA, signA, axisB, signB) && !isBlockedAlongEdge(cells, cell, axisA, signA, axisB, signB)) {
            type = isStep(cells, cell, axisA, signA, axisB, signB) ? STEP_CUT : TIP_CUT;
          } else if (isInnerCorner(cells, cell, axisA, signA, axisB, signB)) {
            type = INNER_CUT;
          }
          int edge = plane * EDGES_PER_PLANE + (signA > 0 ? 1 : 0) + (signB > 0 ? 2 : 0);
          mask |= type << (edge * EDGE_BITS);
        }
      }
    }
    return mask;
  }

  /**
   * Whether the edge is an inner corner: crystals on both sides, but air diagonally behind it. The neighbours are inset towards that air,
   * so the corner would stick out.
   */
  private static boolean isInnerCorner(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    return get(cells, cell, axisA, signA, axisB, 0, 0) == CRYSTAL && get(cells, cell, axisA, 0, axisB, signB, 0) == CRYSTAL
        && get(cells, cell, axisA, signA, axisB, signB, 0) == EMPTY;
  }

  private static boolean isOpen(int classification) {
    return classification == EMPTY;
  }

  private static boolean isOpenEdge(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    return isOpen(get(cells, cell, axisA, signA, axisB, 0, 0)) && isOpen(get(cells, cell, axisA, 0, axisB, signB, 0));
  }

  /**
   * Whether the edge is a step of a staircase: open on both sides while the opposite edge has crystals on both sides.
   */
  static boolean isStep(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    return isOpenEdge(cells, cell, axisA, signA, axisB, signB) && get(cells, cell, axisA, -signA, axisB, 0, 0) == CRYSTAL
        && get(cells, cell, axisA, 0, axisB, -signB, 0) == CRYSTAL;
  }

  /**
   * Whether the edge would be cut considering only the cell itself.
   */
  private static boolean wouldCut(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    if (!isOpenEdge(cells, cell, axisA, signA, axisB, signB)) {
      return false;
    }
    if (isStep(cells, cell, axisA, signA, axisB, signB)) {
      return true;
    }
    for (int[] plane : PLANES) {
      for (int stepA = -1; stepA <= 1; stepA += 2) {
        for (int stepB = -1; stepB <= 1; stepB += 2) {
          if (isStep(cells, cell, plane[0], stepA, plane[1], stepB)
              && (sharesSide(plane[0], stepA, axisA, signA, axisB, signB) || sharesSide(plane[1], stepB, axisA, signA, axisB, signB))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean sharesSide(int axis, int sign, int axisA, int signA, int axisB, int signB) {
    return axis == axisA && sign == signA || axis == axisB && sign == signB;
  }

  /**
   * Whether a crystal along the edge, reached through other crystals, would not cut the edge.
   */
  private static boolean isBlockedAlongEdge(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    int edgeAxis = 3 - axisA - axisB;
    for (int direction = -1; direction <= 1; direction += 2) {
      int[] neighbor = cell.clone();
      for (int distance = 1; distance <= MAX_EDGE_DISTANCE; distance++) {
        neighbor[edgeAxis] += direction;
        if (cells.get(neighbor[0], neighbor[1], neighbor[2]) != CRYSTAL) {
          break;
        }
        if (!wouldCut(cells, neighbor, axisA, signA, axisB, signB)) {
          return true;
        }
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
   * The cuts of a cell as {@code [axisA, axisB, signA, signB, type]}, where the signs point to the two sides next to the cut edge.
   */
  private List<int[]> cuts(int[] cell) {
    int mask = key.cutMask(cell[0], cell[1], cell[2]);
    List<int[]> cuts = new ArrayList<>();
    for (int plane = 0; plane < PLANES.length; plane++) {
      for (int edge = 0; edge < EDGES_PER_PLANE; edge++) {
        int type = mask >> ((plane * EDGES_PER_PLANE + edge) * EDGE_BITS) & ((1 << EDGE_BITS) - 1);
        if (type != NO_CUT) {
          cuts.add(new int[] {PLANES[plane][0], PLANES[plane][1], (edge & 1) != 0 ? 1 : -1, (edge & 2) != 0 ? 1 : -1, type});
        }
      }
    }
    return cuts;
  }

  private ConvexSolid core(int[] cell) {
    double[] min = new double[3];
    double[] max = new double[3];
    for (int axis = 0; axis < 3; axis++) {
      min[axis] = cell[axis] * SIZE + (get(cell, axis, -1) != EMPTY ? 0 : INSET);
      max[axis] = cell[axis] * SIZE + SIZE - (get(cell, axis, 1) != EMPTY ? 0 : INSET);
    }

    ConvexSolid core = ConvexSolid.box(min, max);
    for (int[] cut : cuts(cell)) {
      int axisA = cut[0];
      int axisB = cut[1];
      int signA = cut[2];
      int signB = cut[3];
      Vector3d normal = new Vector3d().setComponent(axisA, signA).setComponent(axisB, signB);
      Vector3d point = new Vector3d(min[0] + max[0], min[1] + max[1], min[2] + max[2]).div(2);
      switch (cut[4]) {
        case STEP_CUT -> {
          // The slope runs from the open side of one axis to the crystal side of the other, through the corners of the inset box.
          point.setComponent(axisA, signA > 0 ? max[axisA] : min[axisA]);
          point.setComponent(axisB, signB > 0 ? min[axisB] : max[axisB]);
        }
        case INNER_CUT -> {
          // Through the inset sides of both neighbours where they meet this crystal.
          double base = cell[axisA] * SIZE;
          point.setComponent(axisA, base + (signA > 0 ? SIZE - INSET : INSET));
          point.setComponent(axisB, cell[axisB] * SIZE + (signB > 0 ? SIZE : 0));
        }
        default -> {
          // Cut off a triangle with legs of half the box size from the edge. The plane only depends on the two sides next to the edge,
          // so crystals along the edge with the same sides get the same plane.
          double leg = Math.min(max[axisA] - min[axisA], max[axisB] - min[axisB]) / 2;
          point.setComponent(axisA, (signA > 0 ? max[axisA] : min[axisA]) - signA * leg);
          point.setComponent(axisB, signB > 0 ? max[axisB] : min[axisB]);
        }
      }
      core = core.cut(normal, point);
    }
    return core;
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
