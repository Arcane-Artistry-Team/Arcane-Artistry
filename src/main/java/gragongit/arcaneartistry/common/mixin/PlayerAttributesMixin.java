package gragongit.arcaneartistry.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import gragongit.arcaneartistry.common.mana.ManaAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class PlayerAttributesMixin {

  @Inject(method = "createAttributes", at = @At("RETURN"))
  private static void arcaneartistry$addManaAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
    cir.getReturnValue().add(ManaAttributes.MAX_MANA);
  }
}
