package gragongit.arcaneartistry.client.staff;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec2;

public interface MouseInputCallback {
  Event<MouseInputCallback> EVENT = EventFactory.createArrayBacked(MouseInputCallback.class, listeners -> (delta) -> {
    for (MouseInputCallback listener : listeners) {
      InteractionResult result = listener.onMouseInput(delta);

      if (result != InteractionResult.PASS) {
        return result;
      }
    }

    return InteractionResult.PASS;
  });

  InteractionResult onMouseInput(Vec2 delta);
}
