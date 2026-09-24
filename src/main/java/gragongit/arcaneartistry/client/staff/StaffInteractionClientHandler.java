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

    if (accDelta.length() >= INPUT_THRESHOLD) {
      sendStroke(resolveDirection(accDelta));
      accDelta = Vec2.ZERO;
    }

    state.setAccumulatedDelta(accDelta);

    Vec2 offset = clampComponents(state.getStaffRenderOffset().add(delta.scale(NORMALIZE_SCALE)), -1F, 1F);
    state.setStaffRenderOffset(offset);
    offsetDirty = true;

    return InteractionResult.CONSUME;
  }

  private static StaffDirection resolveDirection(Vec2 delta) {
    double angle = Math.atan2(delta.y, delta.x);
    int directionIndex = ((int) Math.round(angle / (Math.PI / (StaffDirection.values().length / 2)))) & StaffDirection.values().length - 1;
    return StaffDirection.values()[directionIndex];
  }

  private static Vec2 clampComponents(Vec2 v, float min, float max) {
    return new Vec2(Mth.clamp(v.x, min, max), Mth.clamp(v.y, min, max));
  }

  private static void onClientTick(Minecraft client) {
    if (client.level == null) {
      return;
    }

    for (Player player : client.level.players()) {
      CastState state = CastState.of(player);
      state.setStaffRenderOffsetOld(state.getStaffRenderOffset());
    }

    Player player = client.player;
    if (player == null || !offsetDirty) {
      return;
    }

    CastState state = CastState.of(player);
    ClientPlayNetworking.send(new StaffRenderOffsetPayload(state.getStaffRenderOffset()));
    offsetDirty = false;
  }

  private static void sendStroke(StaffDirection direction) {
    ClientPlayNetworking.send(new StrokePayload(direction));
  }
}
