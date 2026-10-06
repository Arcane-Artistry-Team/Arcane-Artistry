package gragongit.arcaneartistry.client.guidebook.page;

import java.util.Optional;
import net.minecraft.network.chat.Component;

/** What a book page shows, before it is split into display pages. */
public record PageContent(Optional<Component> title, Optional<PageVisual> visual, Optional<Component> text) {
}
