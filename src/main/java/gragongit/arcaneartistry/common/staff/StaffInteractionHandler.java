package gragongit.arcaneartistry.common.staff;

import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.api.CastProgressEvents;
import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.api.CastState;
import gragongit.arcaneartistry.common.network.StaffRenderOffsetPayload;
import gragongit.arcaneartistry.common.network.StrokePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;

public final class StaffInteractionHandler {

  public static void register() {
    StaffInteractionEvents.START.register(StaffInteractionHandler::onStaffInteractionStart);
    StaffInteractionEvents.HOLD.register(StaffInteractionHandler::onStaffInteractionHold);
    StaffInteractionEvents.STOP.register(StaffInteractionHandler::onStaffInteractionStop);
    ServerPlayNetworking
        .registerGlobalReceiver(StrokePayload.TYPE, (payload,
            context) -> context.server().execute(() -> StaffInteractionHandler.appendStroke(context.player(), payload.direction())));
    ServerPlayNetworking
        .registerGlobalReceiver(StaffRenderOffsetPayload.TYPE,
            (payload, context) -> context.server().execute(() -> StaffInteractionHandler.updateRenderOffset(context.player(), payload)));
  }

  public static void onStaffInteractionStart(Player player) {
    CastState state = CastState.of(player);
    state.setAccumulatedYaw(0);
    state.setAccumulatedPitch(0);
    state.setStaffRenderOffsetYaw(0);
    state.setStaffRenderOffsetPitch(0);
    state.clearStrokes();
    state.setCasting(true);

    player.startUsingItem(player.getUsedItemHand());
    CastProgressEvents.START.invoker().onCastProgressStart(getCastProgressContext(player));
  }

  static void onStaffInteractionHold(Player player) {
    CastProgressEvents.HOLD.invoker().onCastProgressHold(getCastProgressContext(player));
  }

  private static void appendStroke(Player player, StaffDirection direction) {
    CastState state = CastState.of(player);
    CastPattern strokes = state.getStrokes();

    if (!strokes.isEmpty() && strokes.getLast() == direction) {
      return;
    }

    state.addStroke(direction);
    CastProgressEvents.STROKE_ADDED.invoker().onCastProgressStrokeAdded(getCastProgressContext(player));
  }

  public static void onStaffInteractionStop(Player player) {
    CastState.of(player).setCasting(false);
    CastProgressEvents.STOP.invoker().onCastProgressStop(getCastProgressContext(player));
  }

  public static void cancel(Player player) {
    CastState state = CastState.of(player);
    state.setCasting(false);
    state.clearStrokes();
  }

  private static void updateRenderOffset(Player player, StaffRenderOffsetPayload payload) {
    CastState state = CastState.of(player);
    if (!state.isCasting()) {
      return;
    }
    state.setStaffRenderOffsetYaw(payload.yaw());
    state.setStaffRenderOffsetPitch(payload.pitch());
  }

  private static CastProgressContext getCastProgressContext(Player player) {
    return new CastProgressContext(player, CastState.of(player).getStrokes());
  }
}
