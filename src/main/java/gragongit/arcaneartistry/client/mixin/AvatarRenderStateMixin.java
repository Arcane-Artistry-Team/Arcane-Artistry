package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import gragongit.arcaneartistry.client.renderer.CastingRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements CastingRenderState {
  @Unique
  private boolean arcaneArtistry$casting;
  @Unique
  private float arcaneArtistry$yaw;
  @Unique
  private float arcaneArtistry$pitch;

  @Override
  public boolean arcaneArtistry$isCasting() {
    return arcaneArtistry$casting;
  }

  @Override
  public void arcaneArtistry$setCasting(boolean casting) {
    this.arcaneArtistry$casting = casting;
  }

  @Override
  public float arcaneArtistry$getYaw() {
    return arcaneArtistry$yaw;
  }

  @Override
  public void arcaneArtistry$setYaw(float yaw) {
    this.arcaneArtistry$yaw = yaw;
  }

  @Override
  public float arcaneArtistry$getPitch() {
    return arcaneArtistry$pitch;
  }

  @Override
  public void arcaneArtistry$setPitch(float pitch) {
    this.arcaneArtistry$pitch = pitch;
  }
}
