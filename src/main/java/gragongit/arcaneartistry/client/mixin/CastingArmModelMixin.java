package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import gragongit.arcaneartistry.client.renderer.CastingRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(HumanoidModel.class)
public abstract class CastingArmModelMixin<T extends HumanoidRenderState> {

  @Shadow
  public ModelPart rightArm;
  @Shadow
  public ModelPart leftArm;
  @Unique
  private static final float MAX_ROTATION = 0.6F;

  @Inject(method = "setupAnim", at = @At("TAIL"))
  private void arcaneartistry$applyStaffSwing(T state, CallbackInfo ci) {
    if (!(state instanceof CastingRenderState castingState) || !castingState.arcaneArtistry$isCasting()) {
      return;
    }

    HumanoidArm arm = state.useItemHand == InteractionHand.MAIN_HAND ? state.mainArm : state.mainArm.getOpposite();
    ModelPart armPart = arm == HumanoidArm.RIGHT ? rightArm : leftArm;

    float yaw = Mth.clamp(castingState.arcaneArtistry$getYaw(), -MAX_ROTATION, MAX_ROTATION);
    float pitch = Mth.clamp(castingState.arcaneArtistry$getPitch(), -MAX_ROTATION, MAX_ROTATION);

    armPart.xRot += pitch;
    armPart.yRot += (arm == HumanoidArm.RIGHT ? 1 : -1) * yaw;
  }
}
