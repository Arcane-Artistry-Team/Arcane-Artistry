package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gragongit.arcaneartistry.common.api.CastState;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class CastingHandMovementMixin {
  @Unique
  private static final float MAX_STAFF_MOVEMENT = 0.25F;
  @Unique
  private static final float STAFF_CENTER_POS_X = 0.25F;
  @Unique
  private static final float STAFF_CENTER_POS_Y = -0.15F;
  @Unique
  private static final float STAFF_CENTER_POS_Z = -0.3F;
  @Unique
  private static final float STAFF_ROT_X = -70.0F;
  @Unique
  private static final float STAFF_ROT_Y = 10.0F;

  @Inject(method = "renderItem", at = @At("HEAD"))
  private void arcaneartistry$applyStaffCursorOffset(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
    if (!type.firstPerson() || !(mob instanceof Player player)) {
      return;
    }

    CastState state = CastState.of(player);
    if (!state.isCasting()) {
      return;
    }

    InteractionHand castingHand = player.getUsedItemHand();
    if (itemStack != player.getItemInHand(castingHand)) {
      return;
    }

    if (!player.isUsingItem() || player.getUseItemRemainingTicks() <= 0) {
      return;
    }

    HumanoidArm arm = castingHand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
    int invert = arm == HumanoidArm.RIGHT ? 1 : -1;

    float offsetX = state.getStaffRenderOffsetYaw() * MAX_STAFF_MOVEMENT;
    float offsetY = state.getStaffRenderOffsetPitch() * MAX_STAFF_MOVEMENT;
    poseStack.translate(STAFF_CENTER_POS_X + offsetX, STAFF_CENTER_POS_Y + -offsetY, STAFF_CENTER_POS_Z);
    poseStack.mulPose(Axis.XP.rotationDegrees(STAFF_ROT_X));
    poseStack.mulPose(Axis.YP.rotationDegrees(invert * STAFF_ROT_Y));
  }
}
