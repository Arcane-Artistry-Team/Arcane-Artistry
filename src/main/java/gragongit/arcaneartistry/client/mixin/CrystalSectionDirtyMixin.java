package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import gragongit.arcaneartistry.common.block.CrystalBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Whether a crystal is cut diagonally depends on blocks up to two blocks away, and the faces of a crystal depend on how its neighbours are
 * cut. A crystal's geometry thus depends on blocks three blocks away. Vanilla only rebuilds the sections within one block of a change, so
 * the radius is widened to three blocks around crystals.
 *
 * <p>
 * The geometry only depends on blocks that touch a crystal, at least diagonally, so changes to other blocks are ignored.
 */
@Mixin(ClientLevel.class)
public abstract class CrystalSectionDirtyMixin {
  private static final int CRYSTAL_RADIUS = 3;

  @Inject(method = "setBlocksDirty", at = @At("TAIL"))
  private void dirtyCrystalNeighborhood(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
    ClientLevel level = (ClientLevel) (Object) this;
    if (!isCrystal(oldState) && !isCrystal(newState) && !hasCrystalNeighbor(level, pos)) {
      return;
    }

    level
        .setSectionRangeDirty(SectionPos.blockToSectionCoord(pos.getX() - CRYSTAL_RADIUS),
            SectionPos.blockToSectionCoord(pos.getY() - CRYSTAL_RADIUS), SectionPos.blockToSectionCoord(pos.getZ() - CRYSTAL_RADIUS),
            SectionPos.blockToSectionCoord(pos.getX() + CRYSTAL_RADIUS), SectionPos.blockToSectionCoord(pos.getY() + CRYSTAL_RADIUS),
            SectionPos.blockToSectionCoord(pos.getZ() + CRYSTAL_RADIUS));
  }

  private static boolean isCrystal(BlockState state) {
    return state.getBlock() instanceof CrystalBlock;
  }

  private static boolean hasCrystalNeighbor(ClientLevel level, BlockPos pos) {
    BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          if ((x != 0 || y != 0 || z != 0) && isCrystal(level.getBlockState(neighbor.setWithOffset(pos, x, y, z)))) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
