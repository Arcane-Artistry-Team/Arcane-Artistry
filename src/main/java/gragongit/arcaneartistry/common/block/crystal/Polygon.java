package gragongit.arcaneartistry.common.block.crystal;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.Nullable;

/**
 * A planar convex polygon whose vertices are ordered counter-clockwise when viewed from the side its {@link #normal} points to.
 */
public record Polygon(List<Vector3dc> vertices, Vector3dc normal) {
  public static final double EPSILON = 1e-6;

  /**
   * Creates a square of the given half size lying on the plane {@code normal · p = offset}.
   */
  static Polygon square(Vector3dc normal, double offset, double halfSize) {
    Vector3d helper = Math.abs(normal.x()) < 0.9 ? new Vector3d(1, 0, 0) : new Vector3d(0, 1, 0);
    Vector3d tangent = helper.cross(normal, new Vector3d()).normalize().mul(halfSize);
    Vector3d bitangent = normal.cross(tangent, new Vector3d());
    Vector3d center = normal.mul(offset, new Vector3d());
    return new Polygon(List
        .of(center.sub(tangent, new Vector3d()).sub(bitangent), center.add(tangent, new Vector3d()).sub(bitangent),
            center.add(tangent, new Vector3d()).add(bitangent), center.sub(tangent, new Vector3d()).add(bitangent)),
        normal);
  }

  double minDistance(Vector3dc planeNormal, double offset) {
    double min = Double.POSITIVE_INFINITY;
    for (Vector3dc vertex : vertices) {
      min = Math.min(min, planeNormal.dot(vertex) - offset);
    }
    return min;
  }

  double maxDistance(Vector3dc planeNormal, double offset) {
    double max = Double.NEGATIVE_INFINITY;
    for (Vector3dc vertex : vertices) {
      max = Math.max(max, planeNormal.dot(vertex) - offset);
    }
    return max;
  }

  /**
   * Returns the part of this polygon on the inner side ({@code planeNormal · p <= offset}) of the plane, or null if nothing remains.
   */
  @Nullable
  Polygon clip(Vector3dc planeNormal, double offset) {
    return split(planeNormal, offset)[0];
  }

  /**
   * Splits this polygon by a plane. Index 0 holds the inner part ({@code planeNormal · p <= offset}), index 1 the outer part. Parts that
   * are empty or degenerate are null.
   */
  @Nullable
  Polygon[] split(Vector3dc planeNormal, double offset) {
    List<Vector3dc> inner = new ArrayList<>(vertices.size() + 1);
    List<Vector3dc> outer = new ArrayList<>(vertices.size() + 1);
    int count = vertices.size();
    for (int i = 0; i < count; i++) {
      Vector3dc current = vertices.get(i);
      Vector3dc next = vertices.get((i + 1) % count);
      double currentDistance = planeNormal.dot(current) - offset;
      double nextDistance = planeNormal.dot(next) - offset;
      int currentSide = side(currentDistance);
      int nextSide = side(nextDistance);

      if (currentSide <= 0) {
        inner.add(current);
      }
      if (currentSide >= 0) {
        outer.add(current);
      }
      if (currentSide * nextSide < 0) {
        Vector3dc intersection = current.lerp(next, currentDistance / (currentDistance - nextDistance), new Vector3d());
        inner.add(intersection);
        outer.add(intersection);
      }
    }
    return new Polygon[] {create(inner), create(outer)};
  }

  private static int side(double distance) {
    return distance > EPSILON ? 1 : distance < -EPSILON ? -1 : 0;
  }

  @Nullable
  private Polygon create(List<Vector3dc> points) {
    List<Vector3dc> cleaned = clean(points);
    if (cleaned.size() < 3) {
      return null;
    }
    Polygon polygon = new Polygon(cleaned, normal);
    return polygon.area() > EPSILON ? polygon : null;
  }

  /**
   * Removes duplicate and collinear vertices.
   */
  private static List<Vector3dc> clean(List<Vector3dc> points) {
    List<Vector3dc> result = new ArrayList<>(points);
    boolean changed = true;
    while (changed && result.size() >= 3) {
      changed = false;
      for (int i = 0; i < result.size(); i++) {
        Vector3dc previous = result.get((i + result.size() - 1) % result.size());
        Vector3dc current = result.get(i);
        Vector3dc next = result.get((i + 1) % result.size());
        Vector3d toCurrent = current.sub(previous, new Vector3d());
        Vector3d toNext = next.sub(current, new Vector3d());
        if (toCurrent.lengthSquared() < EPSILON * EPSILON || toCurrent.cross(toNext).length() < EPSILON) {
          result.remove(i);
          changed = true;
          break;
        }
      }
    }
    return result;
  }

  double area() {
    Vector3d sum = new Vector3d();
    Vector3d cross = new Vector3d();
    int count = vertices.size();
    for (int i = 0; i < count; i++) {
      sum.add(vertices.get(i).cross(vertices.get((i + 1) % count), cross));
    }
    return Math.abs(sum.dot(normal)) / 2;
  }
}
