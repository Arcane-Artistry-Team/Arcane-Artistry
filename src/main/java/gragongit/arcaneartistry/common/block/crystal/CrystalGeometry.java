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
import org.jspecify.annotations.Nullable;
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
 * <p>The interior of the crystal is built the same way with an inset of {@link #INTERIOR_INSET} pixels, which shrinks the whole crystal
 * by half a block. Crystals that are only one block wide, with no crystal on either side along an axis, have no interior. The interiors
 * of their neighbours end at them like at solid blocks.
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
  /**
   * How far a tip cut reaches into the two sides next to the edge: half of an inset side, so two opposite tip cuts meet in its center. The
   * same holds for the interior of a crystal two blocks wide.
   */
  private static final double TIP_LEG = (SIZE - 2 * INSET) / 2D;
  /**
   * The inset of the interior: the crystal shrunk by half a block.
   */
  private static final int INTERIOR_INSET = INSET + SIZE / 2;

  private final Key key;
  private final boolean interior;
  private final int inset;
  private final List<ConvexSolid> solids = new ArrayList<>();

  private CrystalGeometry(Key key, boolean interior) {
    this.key = key;
    this.interior = interior;
    this.inset = interior ? INTERIOR_INSET : INSET;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          // Crystals diagonally next to the cell only touch it along an edge or in a corner, so they cannot cover any of its faces.
          if (Math.abs(x) + Math.abs(y) + Math.abs(z) <= 1 && get(x, y, z) == CRYSTAL) {
            int[] cell = {x, y, z};
            ConvexSolid core = core(cell);
            if (core != null) {
              add(core);
            }
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
   * Whether all 26 cells around the center of {@code cells} are crystals. Such a crystal has no visible surface, neither outside nor in
   * its interior.
   */
  public static boolean isEnclosed(Cells cells) {
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          if (cells.get(x, y, z) != CRYSTAL) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * The surface of the crystal and of its interior inside the cell, split for rendering as described in {@link #splitAtCenter}.
   */
  public record Result(List<Polygon> surface, List<Polygon> interior, long[][] rubbleMargins) {}

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
   * <li>If the crystals continuing along the edge do not cut it, the edge is not cut either, so the cut does not run into their side. If
   * they are not steps there, a step is cut as a tip like them.</li>
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
            type = isStep(cells, cell, axisA, signA, axisB, signB) && isStepAlongEdge(cells, cell, axisA, signA, axisB, signB)
                ? STEP_CUT
                : TIP_CUT;
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

  /**
   * Whether all crystals continuing along the edge are steps there as well. Otherwise the slope would end at their side, so the edge is
   * cut as a tip like theirs.
   */
  private static boolean isStepAlongEdge(Cells cells, int[] cell, int axisA, int signA, int axisB, int signB) {
    int edgeAxis = 3 - axisA - axisB;
    for (int direction = -1; direction <= 1; direction += 2) {
      int[] neighbor = cell.clone();
      for (int distance = 1; distance <= MAX_EDGE_DISTANCE; distance++) {
        neighbor[edgeAxis] += direction;
        if (cells.get(neighbor[0], neighbor[1], neighbor[2]) != CRYSTAL) {
          break;
        }
        if (!isStep(cells, neighbor, axisA, signA, axisB, signB)) {
          return false;
        }
      }
    }
    return true;
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
    return new CrystalGeometry(key, false).build();
  }

  static CrystalGeometry of(Key key) {
    return new CrystalGeometry(key, false);
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

  /**
   * The surface of the crystal inside the cell.
   */
  List<Polygon> surface() {
    return ConvexSolid.unionSurface(solids, 0, SIZE);
  }

  private Result build() {
    List<Polygon> surface = surface();
    long[][] margins = new long[6][];
    if (get(0, 0, 0) == CRYSTAL) {
      for (Direction direction : Direction.values()) {
        if (get(direction.getStepX(), direction.getStepY(), direction.getStepZ()) == SOLID) {
          margins[direction.ordinal()] = rubbleMargin(direction);
        }
      }
    }
    List<Polygon> interior = isThin(new int[3]) ? List.of() : new CrystalGeometry(key, true).interior(surface);
    if (interior.isEmpty()) {
      return new Result(surface, interior, margins);
    }
    return new Result(splitAtCenter(surface), splitAtCenter(interior), margins);
  }

  /**
   * Splits the polygons at the center of the cell along each axis. Translucent quads are sorted by their centers, which fails for a large
   * quad in front of a small one: the surface of the crystal can be drawn before its interior and hide it through the depth buffer. As the
   * interior is inset by half a block, the pieces of the surface and of the interior then lie almost exactly in front of each other.
   * Polygons spanning the whole cell along an axis are not split along it, as the surface and the interior both do so there.
   */
  private static List<Polygon> splitAtCenter(List<Polygon> polygons) {
    List<Polygon> pieces = new ArrayList<>(polygons);
    for (int axis = 0; axis < 3; axis++) {
      Vector3dc normal = new Vector3d().setComponent(axis, 1);
      List<Polygon> split = new ArrayList<>();
      for (Polygon piece : pieces) {
        double min = piece.minDistance(normal, SIZE / 2D);
        double max = piece.maxDistance(normal, SIZE / 2D);
        if (min < -Polygon.EPSILON && max > Polygon.EPSILON && (min > Polygon.EPSILON - SIZE / 2D || max < SIZE / 2D - Polygon.EPSILON)) {
          for (Polygon part : piece.split(normal, SIZE / 2D)) {
            if (part != null) {
              split.add(part);
            }
          }
        } else {
          split.add(piece);
        }
      }
      pieces = split;
    }
    return pieces;
  }

  /**
   * The surface of this geometry as the interior of the crystal with the given {@code surface}. Where the interior touches the surface of
   * the crystal, for example where it grows out of a solid block, the two would coincide and flicker, so the interior is left out there.
   */
  private List<Polygon> interior(List<Polygon> surface) {
    List<Polygon> interior = new ArrayList<>();
    for (Polygon polygon : surface()) {
      List<Polygon> fragments = List.of(polygon);
      for (Polygon outer : surface) {
        if (outer.isCoplanar(polygon)) {
          List<Polygon> remaining = new ArrayList<>();
          for (Polygon fragment : fragments) {
            fragment.subtract(outer, remaining);
          }
          fragments = remaining;
        }
      }
      interior.addAll(fragments);
    }
    return interior;
  }

  /**
   * Whether the crystal at {@code cell} is only one block wide: along some axis, neither side is a crystal. Cells outside the 3x3x3 cells
   * of the {@link Key} are not known and assumed to be crystals.
   */
  private boolean isThin(int[] cell) {
    for (int axis = 0; axis < 3; axis++) {
      if (cell[axis] == 0 && get(cell, axis, -1) != CRYSTAL && get(cell, axis, 1) != CRYSTAL) {
        return true;
      }
    }
    return false;
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

  /**
   * The core box of the crystal at {@code cell} with its cuts, or null if there is none.
   */
  private @Nullable ConvexSolid core(int[] cell) {
    if (interior && isThin(cell)) {
      return null;
    }
    double[] min = new double[3];
    double[] max = new double[3];
    for (int axis = 0; axis < 3; axis++) {
      min[axis] = cell[axis] * SIZE + (get(cell, axis, -1) != EMPTY ? 0 : inset);
      max[axis] = cell[axis] * SIZE + SIZE - (get(cell, axis, 1) != EMPTY ? 0 : inset);
      if (min[axis] >= max[axis]) {
        return null;
      }
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
        // Through the inset sides of both neighbours where they meet this crystal.
        case INNER_CUT -> point.set(innerCornerPoint(cell, axisA, signA, axisB, signB));
        default -> {
          // Cut off a triangle with legs of half the inset block size from the edge. The plane only depends on the two sides next to the
          // edge, so crystals along the edge get the same plane even if their other sides differ.
          point.setComponent(axisA, (signA > 0 ? max[axisA] : min[axisA]) - signA * TIP_LEG);
          point.setComponent(axisB, signB > 0 ? max[axisB] : min[axisB]);
        }
      }
      core = core.cut(normal, point);
    }
    if (interior) {
      core = cutSolidCorners(cell, core);
    }
    return taper(cell, core, min, max);
  }

  /**
   * A point on the plane of an {@link #INNER_CUT}, which runs through the inset sides of the neighbours next to the edge.
   */
  private Vector3d innerCornerPoint(int[] cell, int axisA, int signA, int axisB, int signB) {
    return new Vector3d()
        .setComponent(axisA, cell[axisA] * SIZE + (signA > 0 ? SIZE - inset : inset))
        .setComponent(axisB, cell[axisB] * SIZE + (signB > 0 ? SIZE : 0));
  }

  /**
   * Cuts the interior like an inner corner at edges with a solid block on one side, a crystal on the other and air diagonally behind. The
   * crystal is inset towards the air, so the surface steps back next to the solid block, and the interior growing out of the solid block
   * would reach up to that step.
   */
  private ConvexSolid cutSolidCorners(int[] cell, ConvexSolid core) {
    for (int[] plane : PLANES) {
      for (int signA = -1; signA <= 1; signA += 2) {
        for (int signB = -1; signB <= 1; signB += 2) {
          int sideA = get(cell, plane[0], signA);
          int sideB = get(cell, plane[1], signB);
          int[] diagonal = offset(cell, plane[0], signA, plane[1], signB, 0);
          if ((sideA == SOLID && sideB == CRYSTAL || sideA == CRYSTAL && sideB == SOLID) && isKnown(diagonal) && get(diagonal) == EMPTY) {
            Vector3d normal = new Vector3d().setComponent(plane[0], signA).setComponent(plane[1], signB);
            core = core.cut(normal, innerCornerPoint(cell, plane[0], signA, plane[1], signB));
          }
        }
      }
    }
    return core;
  }

  /**
   * Whether the cell lies within the 3x3x3 cells of the {@link Key}.
   */
  private static boolean isKnown(int[] cell) {
    return Math.abs(cell[0]) <= 1 && Math.abs(cell[1]) <= 1 && Math.abs(cell[2]) <= 1;
  }

  /**
   * Bevels the end of an edge whose two sides are open towards a crystal along the edge that cuts it deeper: as a tip where this edge is
   * not cut, or along a slope where this edge is not cut or only cut as a tip. The corner is cut at 45 degrees and meets the deeper cut at
   * the block boundary, so the edge runs smoothly into it instead of ending in a flat step.
   */
  private ConvexSolid taper(int[] cell, ConvexSolid core, double[] min, double[] max) {
    int mask = key.cutMask(cell[0], cell[1], cell[2]);
    for (int plane = 0; plane < PLANES.length; plane++) {
      int axisA = PLANES[plane][0];
      int axisB = PLANES[plane][1];
      int edgeAxis = 3 - axisA - axisB;
      for (int edge = 0; edge < EDGES_PER_PLANE; edge++) {
        int signA = (edge & 1) != 0 ? 1 : -1;
        int signB = (edge & 2) != 0 ? 1 : -1;
        int depth = cutDepth(edgeType(mask, plane, edge));
        if (depth < 0 || get(cell, axisA, signA) != EMPTY || get(cell, axisB, signB) != EMPTY) {
          continue;
        }
        for (int direction = -1; direction <= 1; direction += 2) {
          int[] neighbor = cell.clone();
          neighbor[edgeAxis] += direction;
          if (get(neighbor) != CRYSTAL) {
            continue;
          }
          int neighborType = edgeType(key.cutMask(neighbor[0], neighbor[1], neighbor[2]), plane, edge);
          if (cutDepth(neighborType) <= depth) {
            continue;
          }
          Vector3d normal = new Vector3d().setComponent(axisA, signA).setComponent(axisB, signB).setComponent(edgeAxis, direction);
          Vector3d point = new Vector3d();
          if (neighborType == STEP_CUT) {
            point.setComponent(axisA, signA > 0 ? max[axisA] : min[axisA]);
            point.setComponent(axisB, signB > 0 ? min[axisB] : max[axisB]);
          } else {
            point.setComponent(axisA, (signA > 0 ? max[axisA] : min[axisA]) - signA * TIP_LEG);
            point.setComponent(axisB, signB > 0 ? max[axisB] : min[axisB]);
          }
          point.setComponent(edgeAxis, direction > 0 ? max[edgeAxis] : min[edgeAxis]);
          core = core.cut(normal, point);
        }
      }
    }
    return core;
  }

  /**
   * Orders the cut types by how much they remove from an edge with open sides. Inner corner cuts never apply to such edges.
   */
  private static int cutDepth(int type) {
    return switch (type) {
      case NO_CUT -> 0;
      case TIP_CUT -> 1;
      case STEP_CUT -> 2;
      default -> -1;
    };
  }

  private static int edgeType(int mask, int plane, int edge) {
    return mask >> ((plane * EDGES_PER_PLANE + edge) * EDGE_BITS) & ((1 << EDGE_BITS) - 1);
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
