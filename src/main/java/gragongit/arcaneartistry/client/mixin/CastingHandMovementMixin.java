package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gragongit.arcaneartistry.common.api.CastState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class CastingHandMovementMixin {
  @Unique
  private static final float MAX_STAFF_MOVEMENT = 0.25F;
  @Unique
  private static final float STAFF_CENTER_POS_X = -0.15F;
  @Unique
  private static final float STAFF_CENTER_POS_Y = 0.3F;
  @Unique
  private static final float STAFF_CENTER_POS_Z = -0.5F;
  @Unique
  private static final float STAFF_ROT_X = -60.0F;

  @Inject(method = "submitArmWithItem", at = @At("HEAD"))
  private void arcaneartistry$applyStaffCursorOffset(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state,
      float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }

    CastState castState = CastState.of(player);
    if (!castState.isCasting()) {
      return;
    }

    InteractionHand castingHand = player.getUsedItemHand();
    if (hand != castingHand || itemStack != player.getItemInHand(castingHand)) {
      return;
    }

    if (!player.isUsingItem() || state.useItemRemainingTicks <= 0) {
      return;
    }

    float offsetX = castState.getStaffRenderOffsetYaw() * MAX_STAFF_MOVEMENT;
    float offsetY = castState.getStaffRenderOffsetPitch() * MAX_STAFF_MOVEMENT;

    poseStack.translate(STAFF_CENTER_POS_X + offsetX, STAFF_CENTER_POS_Y + -offsetY, STAFF_CENTER_POS_Z);
    poseStack.rotate(Axis.XP.rotationDegrees(STAFF_ROT_X));
  }
}
