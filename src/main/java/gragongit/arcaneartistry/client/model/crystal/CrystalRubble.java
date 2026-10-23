package gragongit.arcaneartistry.client.model.crystal;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;
import gragongit.arcaneartistry.common.block.crystal.CrystalGeometry;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small debris pieces textured like the solid block a crystal grows out of. They cover the pixels of the contact face that the crystal
 * leaves free because of its inset.
 */
final class CrystalRubble {
  private static final int SIZE = CrystalGeometry.SIZE;
  private static final float PIXEL = 1F / SIZE;
  private static final int GAP_CHANCE = 5;

  private CrystalRubble() {}

  /**
   * Emits the debris for all faces of the crystal at {@code pos}. {@code margins} holds the uncovered pixels of each face as computed by
   * {@link CrystalGeometry}, or null for faces without debris.
   */
  static void emit(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, long[][] margins, RandomSource random) {
    // Debris of faces meeting at an edge would overlap in the corner and flicker, so each pixel of the cell is filled at most once.
    boolean[] occupied = new boolean[SIZE * SIZE * SIZE];
    List<Box> boxes = new ArrayList<>();
    for (Direction direction : Direction.values()) {
      long[] margin = margins[direction.ordinal()];
      if (margin != null) {
        place(boxes, level, pos, state, direction, margin, occupied, random);
      }
    }
    for (Box box : boxes) {
      emitBox(emitter, box, occupied, random);
    }
  }

  /**
   * A debris piece spanning the pixels from {@code min} inclusive to {@code max} exclusive, lying on the solid block towards
   * {@code base}.
   */
  private record Box(int[] min, int[] max, Direction base, Material.Baked material, int color) {}

  private static void place(List<Box> boxes, BlockAndTintGetter level, BlockPos pos, BlockState state, Direction direction, long[] margin,
      boolean[] occupied, RandomSource random) {
    BlockPos neighborPos = pos.relative(direction);
    BlockState neighbor = level.getBlockState(neighborPos);
    Material.Baked material = Minecraft
        .getInstance()
        .getModelManager()
        .getBlockStateModelSet()
        .get(neighbor)
        .particleMaterial(level, neighborPos, neighbor);
    BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(neighbor, 0);
    int color = tintSource != null ? ARGB.opaque(tintSource.colorAsTerrainParticle(neighbor, level, neighborPos)) : -1;

    int axis = direction.getAxis().ordinal();
    int axisU = axis == 0 ? 1 : 0;
    int axisV = axis == 2 ? 1 : 2;
    boolean positive = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;

    random.setSeed(state.getSeed(pos) * 31 + direction.ordinal());
    boolean[] used = new boolean[SIZE * SIZE];
    for (int v = 0; v < SIZE; v++) {
      for (int u = 0; u < SIZE; u++) {
        if (!isFree(margin, used, u, v) || random.nextInt(GAP_CHANCE) == 0) {
          continue;
        }

        int width = 1 + random.nextInt(2);
        int length = 1 + random.nextInt(2);
        if (!fits(margin, used, u, v, width, length)) {
          width = 1;
          length = 1;
        }
        for (int du = 0; du < width; du++) {
          for (int dv = 0; dv < length; dv++) {
            used[(v + dv) * SIZE + u + du] = true;
          }
        }
        int height = 1 + random.nextInt(width * length > 1 ? 3 : 2);
        int[] pixel = new int[3];
        for (int depth = 0; depth < height; depth++) {
          pixel[axis] = positive ? SIZE - 1 - depth : depth;
          if (!occupy(occupied, pixel, axisU, u, width, axisV, v, length)) {
            height = depth;
            break;
          }
        }
        if (height == 0) {
          continue;
        }

        int[] min = new int[3];
        int[] max = new int[3];
        min[axisU] = u;
        min[axisV] = v;
        min[axis] = positive ? SIZE - height : 0;
        max[axisU] = u + width;
        max[axisV] = v + length;
        max[axis] = positive ? SIZE : height;
        boxes.add(new Box(min, max, direction, material, color));
      }
    }
  }

  /**
   * Marks the rectangle of {@code width} by {@code length} pixels at {@code u}, {@code v} in the layer of {@code pixel} as occupied, if
   * none of them is occupied yet.
   */
  private static boolean occupy(boolean[] occupied, int[] pixel, int axisU, int u, int width, int axisV, int v, int length) {
    for (int pass = 0; pass < 2; pass++) {
      for (int du = 0; du < width; du++) {
        for (int dv = 0; dv < length; dv++) {
          pixel[axisU] = u + du;
          pixel[axisV] = v + dv;
          int index = (pixel[0] * SIZE + pixel[1]) * SIZE + pixel[2];
          if (pass == 1) {
            occupied[index] = true;
          } else if (occupied[index]) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean isFree(long[] margin, boolean[] used, int u, int v) {
    if (u >= SIZE || v >= SIZE) {
      return false;
    }
    int bit = v * SIZE + u;
    return (margin[bit >> 6] & (1L << (bit & 63))) != 0 && !used[bit];
  }

  private static boolean fits(long[] margin, boolean[] used, int u, int v, int width, int length) {
    for (int du = 0; du < width; du++) {
      for (int dv = 0; dv < length; dv++) {
        if (!isFree(margin, used, u + du, v + dv)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Emits the faces of a box except the one lying on the solid block and those completely covered by other debris. Each face shows a
   * random part of the texture.
   */
  private static void emitBox(QuadEmitter emitter, Box box, boolean[] occupied, RandomSource random) {
    Vector3f min = new Vector3f(box.min[0], box.min[1], box.min[2]).mul(PIXEL);
    Vector3f max = new Vector3f(box.max[0], box.max[1], box.max[2]).mul(PIXEL);
    for (Direction face : Direction.values()) {
      if (face == box.base || isCovered(box, face, occupied)) {
        continue;
      }

      FaceInfo faceInfo = FaceInfo.fromFacing(face);
      float[] u = new float[4];
      float[] v = new float[4];
      float minU = Float.MAX_VALUE;
      float minV = Float.MAX_VALUE;
      float maxU = -Float.MAX_VALUE;
      float maxV = -Float.MAX_VALUE;
      for (int i = 0; i < 4; i++) {
        Vector3f vertex = faceInfo.getVertexInfo(i).select(min, max);
        emitter.pos(i, vertex);
        CrystalBlockModel.setLockedUv(emitter, i, face, vertex.x, vertex.y, vertex.z);
        u[i] = emitter.u(i);
        v[i] = emitter.v(i);
        minU = Math.min(minU, u[i]);
        minV = Math.min(minV, v[i]);
        maxU = Math.max(maxU, u[i]);
        maxV = Math.max(maxV, v[i]);
      }

      float offsetU = random.nextInt(Math.max(1, Math.round((1 - (maxU - minU)) * SIZE) + 1)) * PIXEL - minU;
      float offsetV = random.nextInt(Math.max(1, Math.round((1 - (maxV - minV)) * SIZE) + 1)) * PIXEL - minV;
      for (int i = 0; i < 4; i++) {
        emitter.uv(i, u[i] + offsetU, v[i] + offsetV);
      }

      boolean onBorder = face.getAxisDirection() == Direction.AxisDirection.POSITIVE
          ? max.get(face.getAxis().ordinal()) >= 1
          : min.get(face.getAxis().ordinal()) <= 0;
      emitter.nominalFace(face).cullFace(onBorder ? face : null).color(box.color, box.color, box.color, box.color);
      emitter.materialBake(box.material, MutableQuadView.BAKE_NORMALIZED).emit();
    }
  }

  /**
   * Whether all pixels directly in front of the face of the box are occupied by debris. Faces on the border of the cell are culled
   * against the neighbouring block instead.
   */
  private static boolean isCovered(Box box, Direction face, boolean[] occupied) {
    int axis = face.getAxis().ordinal();
    int layer = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.max[axis] : box.min[axis] - 1;
    if (layer < 0 || layer >= SIZE) {
      return false;
    }
    int[] pixel = new int[3];
    pixel[axis] = layer;
    int axisU = axis == 0 ? 1 : 0;
    int axisV = axis == 2 ? 1 : 2;
    for (pixel[axisU] = box.min[axisU]; pixel[axisU] < box.max[axisU]; pixel[axisU]++) {
      for (pixel[axisV] = box.min[axisV]; pixel[axisV] < box.max[axisV]; pixel[axisV]++) {
        if (!occupied[(pixel[0] * SIZE + pixel[1]) * SIZE + pixel[2]]) {
          return false;
        }
      }
    }
    return true;
  }
}
