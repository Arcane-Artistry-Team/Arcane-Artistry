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
import net.minecraft.world.phys.Vec2;

public final class StaffInteractionClientHandler {
  private static final double INPUT_THRESHOLD = 200.0;
  private static final float NORMALIZE_SCALE = 0.001F;

  private static boolean offsetDirty;

  public static void register() {
    MouseInputCallback.EVENT.register(StaffInteractionClientHandler::onMouseInput);
    ClientTickEvents.END_CLIENT_TICK.register(StaffInteractionClientHandler::onClientTick);
  }

  private static InteractionResult onMouseInput(Vec2 delta) {
    Player player = Minecraft.getInstance().player;
    if (player == null)
      return InteractionResult.PASS;

    CastState state = CastState.of(player);
    if (!state.isCasting())
      return InteractionResult.PASS;

    Vec2 accDelta = state.getAccumulatedDelta().add(delta);

    if (Math.abs(accDelta.x) >= INPUT_THRESHOLD) {
      sendStroke(accDelta.x > 0 ? StaffDirection.RIGHT : StaffDirection.LEFT);
      accDelta = Vec2.ZERO;
    }
    if (Math.abs(accDelta.y) >= INPUT_THRESHOLD) {
      sendStroke(accDelta.y > 0 ? StaffDirection.DOWN : StaffDirection.UP);
      accDelta = Vec2.ZERO;
    }

    state.setAccumulatedDelta(accDelta);

    float offsetYaw = Mth.clamp(state.getStaffRenderOffsetYaw() + delta.x * NORMALIZE_SCALE, -1F, 1F);
    float offsetPitch = Mth.clamp(state.getStaffRenderOffsetPitch() + delta.y * NORMALIZE_SCALE, -1F, 1F);
    state.setStaffRenderOffsetYaw(offsetYaw);
    state.setStaffRenderOffsetPitch(offsetPitch);
    offsetDirty = true;

    return InteractionResult.CONSUME;
  }

  private static void onClientTick(Minecraft client) {
    if (client.level == null) {
      return;
    }

    for (Player player : client.level.players()) {
      CastState state = CastState.of(player);
      state.setStaffRenderOffsetOld(new Vec2(state.getStaffRenderOffsetYaw(), state.getStaffRenderOffsetPitch()));
    }

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
