package gragongit.arcaneartistry.datagen.guidebook.basics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import gragongit.arcaneartistry.common.registry.ModBlocks;

public class OrbRingEntry extends EntryProvider {
  public static final String ID = "orb_ring";

  public OrbRingEntry(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    page("orb_ring",
        () -> BookSpotlightPageModel.create().withItem(ModBlocks.ORB_RING).withTitle(context().pageTitle()).withText(context().pageText()));
    pageTitle("Orb Ring");
    pageText("The orb ring has to be placed on top of a sturdy block. On its own it does nothing, but it can hold a {0}.",
        entryLink("Lapis Crystal", BasicsCategory.ID, LapisCrystalEntry.ID));
  }

  @Override
  protected String entryName() {
    return "Orb Ring";
  }

  @Override
  protected String entryDescription() {
    return "A golden cradle for a crystal.";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModBlocks.ORB_RING);
  }

  @Override
  protected String entryId() {
    return ID;
  }
}
