package gragongit.arcaneartistry.common.block.crystal;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.Nullable;

/**
 * A convex solid bounded by planes from a fixed set of 26 directions: the 6 axis directions, the 12 diagonals between two axes and the 8
 * space diagonals. This is enough to exactly represent boxes with cut edges and corners.
 *
 * <p>Plane {@code k} bounds the solid by {@code NORMALS[k] · p <= offsets[k]}.
 */
final class ConvexSolid {
  static final Vector3dc[] NORMALS = createNormals();

  private static final double FACE_HALF_SIZE = 128;

  private final double[] offsets;

  private ConvexSolid(double[] offsets) {
    this.offsets = offsets;
  }

  private static Vector3dc[] createNormals() {
    List<Vector3dc> normals = new ArrayList<>();
    for (int axis = 0; axis < 3; axis++) {
      normals.add(new Vector3d().setComponent(axis, 1));
      normals.add(new Vector3d().setComponent(axis, -1));
    }
    for (int first = 0; first < 3; first++) {
      for (int second = first + 1; second < 3; second++) {
        for (int firstSign = -1; firstSign <= 1; firstSign += 2) {
          for (int secondSign = -1; secondSign <= 1; secondSign += 2) {
            normals.add(new Vector3d().setComponent(first, firstSign).setComponent(second, secondSign).normalize());
          }
        }
      }
    }
    for (int corner = 0; corner < 8; corner++) {
      normals.add(new Vector3d((corner & 1) == 0 ? -1 : 1, (corner & 2) == 0 ? -1 : 1, (corner & 4) == 0 ? -1 : 1).normalize());
    }
    return normals.toArray(Vector3dc[]::new);
  }

  /**
   * Creates the convex hull of the given points. The hull is exact as long as all its faces are perpendicular to one of the
   * {@link #NORMALS}.
   */
  static ConvexSolid hull(List<? extends Vector3dc> points) {
    double[] offsets = new double[NORMALS.length];
    for (int k = 0; k < NORMALS.length; k++) {
      double max = Double.NEGATIVE_INFINITY;
      for (Vector3dc point : points) {
        max = Math.max(max, NORMALS[k].dot(point));
      }
      offsets[k] = max;
    }
    return new ConvexSolid(offsets);
  }

  static ConvexSolid box(double[] min, double[] max) {
    return hull(boxCorners(min, max));
  }

  /**
   * Returns this solid cut by the half space {@code normal · p <= normal · point}. The normal must be one of the {@link #NORMALS}, up to
   * its length.
   */
  ConvexSolid cut(Vector3dc normal, Vector3dc point) {
    Vector3d direction = normal.normalize(new Vector3d());
    for (int k = 0; k < NORMALS.length; k++) {
      if (NORMALS[k].distanceSquared(direction) < Polygon.EPSILON) {
        double[] cutOffsets = offsets.clone();
        cutOffsets[k] = Math.min(cutOffsets[k], direction.dot(point));
        return new ConvexSolid(cutOffsets);
      }
    }
    throw new IllegalArgumentException("Unsupported cut normal " + normal);
  }

  static List<Vector3d> boxCorners(double[] min, double[] max) {
    List<Vector3d> corners = new ArrayList<>(8);
    for (int i = 0; i < 8; i++) {
      corners.add(new Vector3d((i & 1) == 0 ? min[0] : max[0], (i & 2) == 0 ? min[1] : max[1], (i & 4) == 0 ? min[2] : max[2]));
    }
    return corners;
  }

  double min(int axis) {
    return -offsets[axis * 2 + 1];
  }

  double max(int axis) {
    return offsets[axis * 2];
  }

  boolean isEmpty() {
    for (int axis = 0; axis < 3; axis++) {
      if (max(axis) - min(axis) <= Polygon.EPSILON) {
        return true;
      }
    }
    return false;
  }

  boolean intersectsBox(double min, double max) {
    for (int axis = 0; axis < 3; axis++) {
      if (max(axis) < min - Polygon.EPSILON || min(axis) > max + Polygon.EPSILON) {
        return false;
      }
    }
    return true;
  }

  /**
   * Whether the point lies inside this solid and at least {@code margin} away from its boundary.
   */
  boolean contains(Vector3dc point, double margin) {
    for (int k = 0; k < NORMALS.length; k++) {
      if (NORMALS[k].dot(point) > offsets[k] - margin) {
        return false;
      }
    }
    return true;
  }

  private List<Polygon> faces() {
    List<Polygon> faces = new ArrayList<>();
    for (int k = 0; k < NORMALS.length; k++) {
      Polygon face = Polygon.square(NORMALS[k], offsets[k], FACE_HALF_SIZE);
      for (int other = 0; other < NORMALS.length && face != null; other++) {
        if (other != k) {
          face = face.clip(NORMALS[other], offsets[other]);
        }
      }
      if (face != null) {
        faces.add(face);
      }
    }
    return faces;
  }

  /**
   * Computes the part of the boundary of the union of all solids that lies inside the box {@code [min, max]^3}. Faces shared by two
   * touching solids are removed so that no inner faces remain.
   *
   * <p>A face lying on the border of the box is only included if it faces out of the box, so that two neighbouring boxes never both
   * include the same face.
   */
  static List<Polygon> unionSurface(List<ConvexSolid> solids, double min, double max) {
    List<Polygon> surface = new ArrayList<>();
    for (int i = 0; i < solids.size(); i++) {
      for (Polygon face : solids.get(i).faces()) {
        Polygon clipped = clipToBox(face, min, max);
        if (clipped == null) {
          continue;
        }

        List<Polygon> fragments = List.of(clipped);
        for (int j = 0; j < solids.size() && !fragments.isEmpty(); j++) {
          if (j == i) {
            continue;
          }
          List<Polygon> remaining = new ArrayList<>();
          for (Polygon fragment : fragments) {
            solids.get(j).subtract(fragment, j > i, remaining);
          }
          fragments = remaining;
        }
        surface.addAll(fragments);
      }
    }
    return surface;
  }

  @Nullable
  private static Polygon clipToBox(Polygon polygon, double min, double max) {
    for (int k = 0; k < 6 && polygon != null; k++) {
      Vector3dc normal = NORMALS[k];
      double offset = k % 2 == 0 ? max : -min;
      if (isCoplanar(polygon, normal, offset)) {
        if (normal.dot(polygon.normal()) <= 0) {
          return null;
        }
      } else {
        polygon = polygon.clip(normal, offset);
      }
    }
    return polygon;
  }

  private static boolean isCoplanar(Polygon polygon, Vector3dc normal, double offset) {
    return polygon.maxDistance(normal, offset) <= Polygon.EPSILON && polygon.minDistance(normal, offset) >= -Polygon.EPSILON;
  }

  /**
   * Adds the parts of {@code polygon} lying outside of this solid to {@code output}. Parts lying on a face of this solid are removed if
   * the face points the other way (touching solids) and kept if it points the same way and {@code keepCoplanar} is set, so that exactly
   * one of two overlapping coplanar faces survives.
   */
  private void subtract(Polygon polygon, boolean keepCoplanar, List<Polygon> output) {
    Polygon inside = polygon;
    for (int k = 0; k < NORMALS.length; k++) {
      Vector3dc normal = NORMALS[k];
      double maxDistance = inside.maxDistance(normal, offsets[k]);
      double minDistance = inside.minDistance(normal, offsets[k]);
      if (maxDistance <= Polygon.EPSILON && minDistance >= -Polygon.EPSILON) {
        if (keepCoplanar && normal.dot(inside.normal()) > 0) {
          output.add(inside);
          return;
        }
        continue;
      }
      if (maxDistance <= Polygon.EPSILON) {
        continue;
      }
      if (minDistance >= -Polygon.EPSILON) {
        output.add(inside);
        return;
      }

      Polygon[] parts = inside.split(normal, offsets[k]);
      if (parts[1] != null) {
        output.add(parts[1]);
      }
      if (parts[0] == null) {
        return;
      }
      inside = parts[0];
    }
  }
}
