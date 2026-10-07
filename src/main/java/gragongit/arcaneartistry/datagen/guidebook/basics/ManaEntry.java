package gragongit.arcaneartistry.datagen.guidebook.basics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class ManaEntry extends EntryProvider {
  public static final String ID = "mana";

  public ManaEntry(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    page("mana", () -> BookTextPageModel.create().withTitle(context().pageTitle()).withText(context().pageText()));
    pageTitle("Mana");
    pageText("""
        Every spell costs mana, and the mana bar shows how much you have left. Mana regenerates over time: slowly at first, then \
        faster the longer you go without casting.

        Without enough mana for a spell, the spell fizzles.""");
  }

  @Override
  protected String entryName() {
    return "Mana";
  }

  @Override
  protected String entryDescription() {
    return "The price of every spell.";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.LAPIS_LAZULI);
  }

  @Override
  protected String entryId() {
    return ID;
  }
}
