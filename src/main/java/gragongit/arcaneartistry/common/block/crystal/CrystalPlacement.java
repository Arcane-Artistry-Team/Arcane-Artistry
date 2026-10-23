package gragongit.arcaneartistry.common.block.crystal;

import java.util.function.Predicate;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Chooses the side of a crystal that blocks are placed against. On a slope, the side hit on the voxelized {@link CrystalShape} depends on
 * which of its small steps is hit. Instead, the side is chosen from the actual surface: among the sides the hit surface faces, the one that
 * is looked at most directly.
 */
public final class CrystalPlacement {
  private static final double EPSILON = 1e-4;

  private CrystalPlacement() {}

  /**
   * The side to place against when looking from {@code eye} towards {@code target}, both relative to the block of the crystal of the given
   * {@linkplain CrystalGeometry#key key}. Returns null if the ray misses the crystal or no side the hit surface faces can be placed on.
   */
  public static @Nullable Direction face(CrystalGeometry.Key key, Vec3 eye, Vec3 target, Predicate<Direction> canPlace) {
    Vector3d from = new Vector3d(eye.x, eye.y, eye.z).mul(CrystalGeometry.SIZE);
    Vector3d direction = new Vector3d(target.x - eye.x, target.y - eye.y, target.z - eye.z).normalize();

    Polygon hit = null;
    double nearest = Double.POSITIVE_INFINITY;
    Vector3d point = new Vector3d();
    for (Polygon polygon : CrystalGeometry.of(key).surface()) {
      double facing = polygon.normal().dot(direction);
      if (facing > -EPSILON) {
        continue;
      }
      double distance = polygon.normal().dot(polygon.vertices().getFirst().sub(from, new Vector3d())) / facing;
      if (distance >= 0 && distance < nearest && polygon.contains(from.fma(distance, direction, point), EPSILON)) {
        nearest = distance;
        hit = polygon;
      }
    }
    if (hit == null) {
      return null;
    }

    Direction best = null;
    double bestFacing = Double.NEGATIVE_INFINITY;
    for (Direction side : Direction.values()) {
      if (hit.normal().dot(side.getStepX(), side.getStepY(), side.getStepZ()) <= EPSILON || !canPlace.test(side)) {
        continue;
      }
      double facing = -direction.dot(side.getStepX(), side.getStepY(), side.getStepZ());
      if (facing > bestFacing) {
        bestFacing = facing;
        best = side;
      }
    }
    return best;
  }
}
