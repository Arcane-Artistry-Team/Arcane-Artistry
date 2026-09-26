package gragongit.arcaneartistry.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import gragongit.arcaneartistry.common.api.CastState;
import gragongit.arcaneartistry.common.staff.StaffInteractionHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(LivingEntity.class)
public abstract class StaffStopUsingMixin {

  @Inject(method = "stopUsingItem", at = @At("HEAD"))
  private void arcaneartistry$cancelCasting(CallbackInfo ci) {
    if ((Object) this instanceof Player player && CastState.of(player).isCasting()) {
      StaffInteractionHandler.cancel(player);
    }
  }
}
