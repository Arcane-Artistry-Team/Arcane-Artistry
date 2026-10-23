package gragongit.arcaneartistry.common.block.crystal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
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
    BitSetDiscreteVoxelShape voxels = new BitSetDiscreteVoxelShape(VOXELS, VOXELS, VOXELS);
    Vector3d point = new Vector3d();
    for (int x = 0; x < VOXELS; x++) {
      for (int y = 0; y < VOXELS; y++) {
        for (int z = 0; z < VOXELS; z++) {
          point.set((x + 0.5) * VOXEL_SIZE, (y + 0.5) * VOXEL_SIZE, (z + 0.5) * VOXEL_SIZE);
          if (geometry.contains(point)) {
            voxels.fill(x, y, z);
          }
        }
      }
    }
    // Like vanilla's block shapes, the voxels are used directly. They are merged into boxes where needed.
    return voxels.isEmpty() ? Shapes.empty() : new CubeVoxelShape(voxels);
  }
}
