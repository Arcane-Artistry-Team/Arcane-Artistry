package gragongit.arcaneartistry.client.staff;

import gragongit.arcaneartistry.common.api.CastState;
import gragongit.arcaneartistry.common.network.StaffRenderOffsetPayload;
import gragongit.arcaneartistry.common.network.StrokePayload;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class StaffInteractionClientHandler {
  private static final double INPUT_THRESHOLD = 200.0;
  private static final double MAX_STAFF_RENDER_OFFSET = 0.5;
  private static final double RENDER_TRANSLATE_SCALE = 0.00075;

  private static boolean offsetDirty;

  public static void register() {
    MouseInputCallback.EVENT.register(StaffInteractionClientHandler::onMouseInput);
    ClientTickEvents.END_CLIENT_TICK.register(StaffInteractionClientHandler::onClientTick);
  }

  private static InteractionResult onMouseInput(double deltaX, double deltaY) {
    Player player = Minecraft.getInstance().player;
    if (player == null)
      return InteractionResult.PASS;

    CastState state = CastState.of(player);
    if (!state.isCasting())
      return InteractionResult.PASS;

    double yaw = state.getAccumulatedYaw() + deltaX;
    double pitch = state.getAccumulatedPitch() + deltaY;

    if (Math.abs(yaw) >= INPUT_THRESHOLD) {
      sendStroke(yaw > 0 ? StaffDirection.RIGHT : StaffDirection.LEFT);
      yaw = 0;
      pitch = 0;
    }
    if (Math.abs(pitch) >= INPUT_THRESHOLD) {
      sendStroke(pitch > 0 ? StaffDirection.DOWN : StaffDirection.UP);
      yaw = 0;
      pitch = 0;
    }

    state.setAccumulatedYaw(yaw);
    state.setAccumulatedPitch(pitch);

    double offsetYaw =
        Mth.clamp(state.getStaffRenderOffsetYaw() + deltaX * RENDER_TRANSLATE_SCALE, -MAX_STAFF_RENDER_OFFSET, MAX_STAFF_RENDER_OFFSET);
    double offsetPitch =
        Mth.clamp(state.getStaffRenderOffsetPitch() + deltaY * RENDER_TRANSLATE_SCALE, -MAX_STAFF_RENDER_OFFSET, MAX_STAFF_RENDER_OFFSET);
    state.setStaffRenderOffsetYaw(offsetYaw);
    state.setStaffRenderOffsetPitch(offsetPitch);
    offsetDirty = true;

    return InteractionResult.CONSUME;
  }

  private static void onClientTick(Minecraft client) {
    Player player = client.player;
    if (player == null || !offsetDirty) {
      return;
    }

    CastState state = CastState.of(player);
    ClientPlayNetworking.send(new StaffRenderOffsetPayload(state.getStaffRenderOffsetYaw(), state.getStaffRenderOffsetPitch()));
    offsetDirty = false;
  }

  private static void sendStroke(StaffDirection direction) {
    ClientPlayNetworking.send(new StrokePayload(direction));
  }
}
