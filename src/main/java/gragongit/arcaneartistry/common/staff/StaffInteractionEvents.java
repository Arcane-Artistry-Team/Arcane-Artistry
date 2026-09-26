package gragongit.arcaneartistry.common.staff;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class StaffInteractionEvents {
  private StaffInteractionEvents() {}

  public interface Start {
    void onStaffInteractionStart(Player player, InteractionHand hand);
  }

  public static final Event<Start> START = EventFactory.createArrayBacked(Start.class, listeners -> (player, hand) -> {
    for (Start listener : listeners) {
      listener.onStaffInteractionStart(player, hand);
    }
  });

  public interface Hold {
    void onStaffInteractionHold(Player player);
  }

  public static final Event<Hold> HOLD = EventFactory.createArrayBacked(Hold.class, listeners -> (player) -> {
    for (Hold listener : listeners) {
      listener.onStaffInteractionHold(player);
    }
  });

  public interface Stop {
    void onStaffInteractionStop(Player player);
  }

  public static final Event<Stop> STOP = EventFactory.createArrayBacked(Stop.class, listeners -> (player) -> {
    for (Stop listener : listeners) {
      listener.onStaffInteractionStop(player);
    }
  });
}
