package gragongit.arcaneartistry.datagen.guidebook;

import com.klikli_dev.modonomicon.api.datagen.book.page.BookPageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import gragongit.arcaneartistry.common.guidebook.SpellPage;
import gragongit.arcaneartistry.common.spell.Spell;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

public class SpellPageModel extends BookPageModel<SpellPageModel> {
  private final ResourceKey<Spell> spell;

  protected SpellPageModel(ResourceKey<Spell> spell) {
    super(SpellPage.ID);
    this.spell = spell;
  }

  public static SpellPageModel create(ResourceKey<Spell> spell) {
    return new SpellPageModel(spell).withId(spell.identifier().getPath());
  }

  @Override
  public BookPage toBookPage(HolderLookup.Provider provider) {
    return new SpellPage(spell, id, condition(provider), associatedItems);
  }
}
