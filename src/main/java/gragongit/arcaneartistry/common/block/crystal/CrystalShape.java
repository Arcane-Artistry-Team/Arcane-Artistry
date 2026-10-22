package gragongit.arcaneartistry.common.block.crystal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The outline and collision shape of a crystal: the crystal's geometry inside its block, voxelized with {@link #VOXEL_SIZE} pixel voxels.
 * Diagonal cuts become small steps.
 */
public final class CrystalShape {
  private static final int SIZE = CrystalGeometry.SIZE;
  private static final int VOXEL_SIZE = 2;
  private static final int VOXELS = SIZE / VOXEL_SIZE;
  private static final int MAX_CACHED_SHAPES = 16384;
  private static final Map<CrystalGeometry.Key, VoxelShape> SHAPES = new ConcurrentHashMap<>();

  private CrystalShape() {}

  /**
   * The shape of the center crystal of the given {@linkplain CrystalGeometry#shapeKey key}.
   */
  public static VoxelShape get(CrystalGeometry.Key key) {
    VoxelShape shape = SHAPES.get(key);
    if (shape == null) {
      if (SHAPES.size() > MAX_CACHED_SHAPES) {
        SHAPES.clear();
      }
      shape = compute(key);
      SHAPES.put(key, shape);
    }
    return shape;
  }

  private static VoxelShape compute(CrystalGeometry.Key key) {
    CrystalGeometry geometry = CrystalGeometry.of(key);
    boolean[] voxels = new boolean[VOXELS * VOXELS * VOXELS];
    Vector3d point = new Vector3d();
    for (int x = 0; x < VOXELS; x++) {
      for (int y = 0; y < VOXELS; y++) {
        for (int z = 0; z < VOXELS; z++) {
          point.set((x + 0.5) * VOXEL_SIZE, (y + 0.5) * VOXEL_SIZE, (z + 0.5) * VOXEL_SIZE);
          voxels[index(x, y, z)] = geometry.contains(point);
        }
      }
    }
    return merge(voxels);
  }

  private static int index(int x, int y, int z) {
    return (x * VOXELS + y) * VOXELS + z;
  }

  /**
   * Greedily merges the voxels into boxes and joins them into a shape.
   */
  private static VoxelShape merge(boolean[] voxels) {
    VoxelShape shape = Shapes.empty();
    for (int x = 0; x < VOXELS; x++) {
      for (int y = 0; y < VOXELS; y++) {
        for (int z = 0; z < VOXELS; z++) {
          if (!voxels[index(x, y, z)]) {
            continue;
          }

          int maxZ = z;
          while (maxZ + 1 < VOXELS && voxels[index(x, y, maxZ + 1)]) {
            maxZ++;
          }
          int maxY = y;
          while (maxY + 1 < VOXELS && isFilled(voxels, x, x, maxY + 1, maxY + 1, z, maxZ)) {
            maxY++;
          }
          int maxX = x;
          while (maxX + 1 < VOXELS && isFilled(voxels, maxX + 1, maxX + 1, y, maxY, z, maxZ)) {
            maxX++;
          }
          for (int i = x; i <= maxX; i++) {
            for (int j = y; j <= maxY; j++) {
              for (int k = z; k <= maxZ; k++) {
                voxels[index(i, j, k)] = false;
              }
            }
          }

          shape = Shapes.joinUnoptimized(shape, Shapes.box(x / (double) VOXELS, y / (double) VOXELS, z / (double) VOXELS,
              (maxX + 1) / (double) VOXELS, (maxY + 1) / (double) VOXELS, (maxZ + 1) / (double) VOXELS), BooleanOp.OR);
        }
      }
    }
    return shape.optimize();
  }

  private static boolean isFilled(boolean[] voxels, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          if (!voxels[index(x, y, z)]) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
