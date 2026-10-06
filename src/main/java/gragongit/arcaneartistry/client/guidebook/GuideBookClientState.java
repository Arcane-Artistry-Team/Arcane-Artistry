package gragongit.arcaneartistry.client.guidebook;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import gragongit.arcaneartistry.common.guidebook.BookCategory;
import net.minecraft.resources.ResourceKey;

/** Where the player left off, so reopening the book returns to the same category and view. */
final class GuideBookClientState {
  record CameraState(double focusX, double focusY, double zoom) {
  }

  static @Nullable ResourceKey<BookCategory> lastCategory;
  static final Map<ResourceKey<BookCategory>, CameraState> CAMERAS = new HashMap<>();

  private GuideBookClientState() {}
}
