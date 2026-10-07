package gragongit.arcaneartistry.datagen.guidebook.basics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class CrystalBallEntry extends EntryProvider {
  public static final String ID = "crystal_ball";

  public CrystalBallEntry(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    page("crystal_ball", () -> BookTextPageModel.create().withTitle(context().pageTitle()).withText(context().pageText()));
    pageTitle("Crystal Ball");
    pageText("""
        Use a {0} on a crystal ball to look into its patterns. Every pattern you have cast with that staff is revealed there, and \
        patterns that form a spell are marked.

        Drag to look around, scroll to zoom and click a pattern to focus it.""", entryLink("staff", BasicsCategory.ID, StaffsEntry.ID));

    page("binding", () -> BookTextPageModel.create().withTitle(context().pageTitle()).withText(context().pageText()));
    pageTitle("Binding a Compendium");
    pageText("Using a book on a crystal ball binds it into a new compendium.");
  }

  @Override
  protected String entryName() {
    return "Crystal Ball";
  }

  @Override
  protected String entryDescription() {
    return "Gaze into the patterns of your staff.";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.ENDER_EYE);
  }

  @Override
  protected String entryId() {
    return ID;
  }
}
