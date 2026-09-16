package gragongit.arcaneartistry.client.staff;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

public interface MouseInputCallback {
  Event<MouseInputCallback> EVENT = EventFactory.createArrayBacked(MouseInputCallback.class, listeners -> (deltaX, deltaY) -> {
    for (MouseInputCallback listener : listeners) {
      InteractionResult result = listener.onMouseInput(deltaX, deltaY);

      if (result != InteractionResult.PASS) {
        return result;
      }
    }

    return InteractionResult.PASS;
  });

  InteractionResult onMouseInput(double deltaX, double deltaY);
}
