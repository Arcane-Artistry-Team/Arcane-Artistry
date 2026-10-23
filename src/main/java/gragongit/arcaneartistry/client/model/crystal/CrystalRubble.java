package gragongit.arcaneartistry.client.model.crystal;

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
import net.minecraft.util.Mth;
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
  static void emit(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, long[][] margins, RandomSource random) {
    // Debris of faces meeting at an edge would overlap in the corner and flicker, so each pixel of the cell is filled at most once.
    boolean[] occupied = new boolean[SIZE * SIZE * SIZE];
    for (Direction direction : Direction.values()) {
      long[] margin = margins[direction.ordinal()];
      if (margin != null) {
        emit(emitter, level, pos, direction, margin, occupied, random);
      }
    }
  }

  private static void emit(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, Direction direction, long[] margin,
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

    random.setSeed(Mth.getSeed(pos.getX(), pos.getY(), pos.getZ()) * 31 + direction.ordinal());
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

        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        min.setComponent(axisU, u * PIXEL).setComponent(axisV, v * PIXEL).setComponent(axis, positive ? 1 - height * PIXEL : 0);
        max.setComponent(axisU, (u + width) * PIXEL).setComponent(axisV, (v + length) * PIXEL).setComponent(axis, positive ? 1 : height * PIXEL);
        emitBox(emitter, min, max, direction, material, color, random);
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
   * Emits all faces of a box except the one touching the solid block. Each face shows a random part of the texture.
   */
  private static void emitBox(QuadEmitter emitter, Vector3f min, Vector3f max, Direction hidden, Material.Baked material, int color,
      RandomSource random) {
    for (Direction face : Direction.values()) {
      if (face == hidden) {
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
      emitter.nominalFace(face).cullFace(onBorder ? face : null).color(color, color, color, color);
      emitter.materialBake(material, MutableQuadView.BAKE_NORMALIZED).emit();
    }
  }
}
