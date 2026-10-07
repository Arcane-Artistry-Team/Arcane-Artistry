package gragongit.arcaneartistry.datagen.guidebook.basics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class StaffsEntry extends EntryProvider {
  public static final String ID = "staffs";

  public StaffsEntry(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    page("casting", () -> BookTextPageModel.create().withTitle(context().pageTitle()).withText(context().pageText()));
    pageTitle("Casting");
    pageText("""
        Some items can serve as a staff. Hold *use* with one to begin casting, then move the mouse in strokes: **up**, **down**, \
        **left** or **right**. Let go to finish the pattern.

        If the pattern matches a spell of that staff, the spell is cast. Every pattern you try is remembered by the {0}.""",
        entryLink("Crystal Ball", BasicsCategory.ID, CrystalBallEntry.ID));
  }

  @Override
  protected String entryName() {
    return "Staffs";
  }

  @Override
  protected String entryDescription() {
    return "Channel spells through motion.";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.BLAZE_ROD);
  }

  @Override
  protected String entryId() {
    return ID;
  }
}
