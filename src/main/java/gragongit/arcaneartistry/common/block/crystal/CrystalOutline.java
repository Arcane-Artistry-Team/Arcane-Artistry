package gragongit.arcaneartistry.common.block.crystal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * The block outline of a crystal: the edges where its surface bends or ends. Unlike the edges of the voxelized {@link CrystalShape}, these
 * follow the slopes and tips of the crystal.
 */
public final class CrystalOutline {
  private static final int MAX_CACHED_OUTLINES = 16384;
  private static final double EPSILON = 1e-4;
  /** Edge coordinates are rounded to this fraction of a pixel. */
  private static final int PRECISION = 10000;
  /** How far next to an edge the surface is probed to find out whether it continues flat across the edge. */
  private static final double PROBE_DISTANCE = 1e-3;
  private static final Map<CrystalGeometry.Key, double[]> OUTLINES = new ConcurrentHashMap<>();

  private CrystalOutline() {}

  /**
   * The edges of the center crystal of the given {@linkplain CrystalGeometry#key key} in block coordinates. Edge {@code i} runs from
   * {@code edges[6i]}, {@code edges[6i + 1]}, {@code edges[6i + 2]} to {@code edges[6i + 3]}, {@code edges[6i + 4]}, {@code edges[6i + 5]}.
   */
  public static double[] get(CrystalGeometry.Key key) {
    double[] outline = OUTLINES.get(key);
    if (outline == null) {
      if (OUTLINES.size() > MAX_CACHED_OUTLINES) {
        OUTLINES.clear();
      }
      outline = compute(key);
      OUTLINES.put(key, outline);
    }
    return outline;
  }

  private static double[] compute(CrystalGeometry.Key key) {
    List<Polygon> surface = CrystalGeometry.of(key).surface();
    Set<Edge> edges = new LinkedHashSet<>();
    for (Polygon polygon : surface) {
      List<Vector3dc> vertices = polygon.vertices();
      for (int i = 0; i < vertices.size(); i++) {
        addVisibleParts(surface, polygon, vertices.get(i), vertices.get((i + 1) % vertices.size()), edges);
      }
    }

    double[] outline = new double[edges.size() * 6];
    int i = 0;
    for (Edge edge : edges) {
      for (long coordinate : new long[] {edge.x1, edge.y1, edge.z1, edge.x2, edge.y2, edge.z2}) {
        outline[i++] = coordinate / (double) PRECISION / CrystalGeometry.SIZE;
      }
    }
    return outline;
  }

  /**
   * Adds the parts of the edge of {@code polygon} from {@code start} to {@code end} across which the surface does not continue flat. The
   * surface is split into many polygons whose edges do not line up, so the edge is split at all vertices lying on it and each part is
   * checked on its own.
   */
  private static void addVisibleParts(List<Polygon> surface, Polygon polygon, Vector3dc start, Vector3dc end, Set<Edge> edges) {
    Vector3d direction = end.sub(start, new Vector3d());
    double length = direction.length();
    direction.div(length);

    List<Double> splits = new ArrayList<>(List.of(0D, length));
    Vector3d offset = new Vector3d();
    for (Polygon other : surface) {
      for (Vector3dc vertex : other.vertices()) {
        vertex.sub(start, offset);
        double t = offset.dot(direction);
        if (t > EPSILON && t < length - EPSILON && offset.fma(-t, direction).lengthSquared() < EPSILON * EPSILON) {
          splits.add(t);
        }
      }
    }
    splits.sort(null);

    Vector3d side = direction.cross(polygon.normal(), new Vector3d()).mul(PROBE_DISTANCE);
    Vector3d middle = new Vector3d();
    for (int i = 0; i + 1 < splits.size(); i++) {
      double from = splits.get(i);
      double to = splits.get(i + 1);
      if (to - from < EPSILON) {
        continue;
      }
      start.fma((from + to) / 2, direction, middle);
      if (!isCovered(surface, polygon.normal(), middle.add(side, new Vector3d()))
          || !isCovered(surface, polygon.normal(), middle.sub(side, new Vector3d()))) {
        edges.add(Edge.of(start.fma(from, direction, new Vector3d()), start.fma(to, direction, new Vector3d())));
      }
    }
  }

  /**
   * Whether a polygon of the surface facing in the direction of {@code normal} contains the point.
   */
  private static boolean isCovered(List<Polygon> surface, Vector3dc normal, Vector3dc point) {
    for (Polygon polygon : surface) {
      if (polygon.normal().dot(normal) > 1 - EPSILON && polygon.contains(point, EPSILON)) {
        return true;
      }
    }
    return false;
  }

  /**
   * An edge with coordinates rounded to {@link #PRECISION}ths of a pixel and ordered endpoints, so the same edge found on both
   * polygons next to it is only drawn once.
   */
  private record Edge(long x1, long y1, long z1, long x2, long y2, long z2) {
    static Edge of(Vector3dc a, Vector3dc b) {
      long ax = round(a.x());
      long ay = round(a.y());
      long az = round(a.z());
      long bx = round(b.x());
      long by = round(b.y());
      long bz = round(b.z());
      boolean ordered = ax != bx ? ax < bx : ay != by ? ay < by : az <= bz;
      return ordered ? new Edge(ax, ay, az, bx, by, bz) : new Edge(bx, by, bz, ax, ay, az);
    }

    private static long round(double coordinate) {
      return Math.round(coordinate * PRECISION);
    }
  }
}
