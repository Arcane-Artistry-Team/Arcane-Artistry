package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import gragongit.arcaneartistry.common.block.CrystalBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Whether a crystal is cut diagonally depends on blocks up to two blocks away, and the faces of a crystal depend on how its neighbours are
 * cut. A crystal's geometry thus depends on blocks three blocks away. Vanilla only rebuilds the sections within one block of a change, so
 * the radius is widened to three blocks around crystals.
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
    for (Direction direction : Direction.values()) {
      if (isCrystal(level.getBlockState(neighbor.setWithOffset(pos, direction)))) {
        return true;
      }
    }
    return false;
  }
}
