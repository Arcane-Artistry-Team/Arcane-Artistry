package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import gragongit.arcaneartistry.client.staff.MouseInputCallback;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;

@Mixin(MouseHandler.class)
public class CastingMouseDeltaMixin {

  @Redirect(method = "turnPlayer(D)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
  private void onPlayerTurn(LocalPlayer player, double deltaX, double deltaY) {
    InteractionResult result = MouseInputCallback.EVENT.invoker().onMouseInput(deltaX, deltaY);
    if (result != InteractionResult.CONSUME) {
      player.turn(deltaX, deltaY);
    }
  }
}
