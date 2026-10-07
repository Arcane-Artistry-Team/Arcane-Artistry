package gragongit.arcaneartistry.datagen.guidebook.basics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import gragongit.arcaneartistry.common.registry.ModItems;

public class LapisCrystalEntry extends EntryProvider {
  public static final String ID = "lapis_crystal";

  public LapisCrystalEntry(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    page("lapis_crystal",
        () -> BookSpotlightPageModel.create().withItem(ModItems.LAPIS_CRYSTAL).withTitle(context().pageTitle()).withText(context().pageText()));
    pageTitle("Lapis Crystal");
    pageText("Using a lapis crystal on an {0} sets it into the ring, creating a **Crystal Ball**.",
        entryLink("Orb Ring", BasicsCategory.ID, OrbRingEntry.ID));
  }

  @Override
  protected String entryName() {
    return "Lapis Crystal";
  }

  @Override
  protected String entryDescription() {
    return "A crystal humming with arcane potential.";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.LAPIS_CRYSTAL);
  }

  @Override
  protected String entryId() {
    return ID;
  }
}
