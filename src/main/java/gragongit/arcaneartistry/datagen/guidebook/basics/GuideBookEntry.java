package gragongit.arcaneartistry.datagen.guidebook.basics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class GuideBookEntry extends EntryProvider {
  public static final String ID = "guide_book";

  public GuideBookEntry(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    page("welcome", () -> BookTextPageModel.create().withTitle(context().pageTitle()).withText(context().pageText()));
    pageTitle("Welcome");
    pageText("""
        This compendium records your arcane studies. Every symbol on the map is an *entry*; click one to read it.

        New entries appear once you have read the entries they build upon. Entries that are {0} still need something from you before \
        they can be read.""", color("locked", 0x8B0000));

    page("binding",
        () -> BookCraftingRecipePageModel.create().withRecipeId1(Identifier.withDefaultNamespace("book")).withText(context().pageText()));
    pageText("The compendium is made from an ordinary book. Use one on an awakened {0} to bind it.",
        entryLink("Crystal Ball", BasicsCategory.ID, CrystalBallEntry.ID));
  }

  @Override
  protected String entryName() {
    return "The Arcane Compendium";
  }

  @Override
  protected String entryDescription() {
    return "A record of everything you have learned about the arcane.";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.ENCHANTED_BOOK);
  }

  @Override
  protected String entryId() {
    return ID;
  }
}
