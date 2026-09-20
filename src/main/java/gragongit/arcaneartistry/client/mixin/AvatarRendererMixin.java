package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import gragongit.arcaneartistry.client.renderer.CastingRenderState;
import gragongit.arcaneartistry.common.api.CastState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

  @Inject(method = "extractRenderState", at = @At("TAIL"))
  private void arcaneartistry$extractCastState(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
    if (!(entity instanceof Player player)) {
      return;
    }

    CastState castState = CastState.of(player);
    CastingRenderState renderState = (CastingRenderState) state;

    renderState.arcaneArtistry$setCasting(castState.isCasting());
    renderState.arcaneArtistry$setYaw(Mth.lerp(partialTicks, castState.getStaffRenderOffsetYawOld(), castState.getStaffRenderOffsetYaw()));
    renderState
        .arcaneArtistry$setPitch(Mth.lerp(partialTicks, castState.getStaffRenderOffsetPitchOld(), castState.getStaffRenderOffsetPitch()));
  }
}
