package gragongit.arcaneartistry.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import gragongit.arcaneartistry.common.mana.ManaAttributes;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModItems;
import gragongit.arcaneartistry.datagen.guidebook.BookContent;
import gragongit.arcaneartistry.datagen.guidebook.GuideBookBootstrap;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class EnglishLanguageProvider extends FabricLanguageProvider {
  private final List<BookContent> bookContent;

  public EnglishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture,
      List<BookContent> bookContent) {
    super(output, "en_us", registriesFuture);
    this.bookContent = bookContent;
  }

  @Override
  public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder translations) {
    translations.add(ModBlocks.ORB_RING, "Orb Ring");
    translations.add(ModItems.LAPIS_CRYSTAL, "Lapis Crystal");
    translations.add(ModItems.GUIDE_BOOK, "Arcane Compendium");

    translations.add(ManaAttributes.MAX_MANA, "Max Mana");

    translations.add("screen.arcane-artistry.crystal_ball", "Crystal Ball");
    translations.add("screen.arcane-artistry.crystal_ball.home", "Home");

    translations.add("screen.arcane-artistry.guide_book", "Arcane Compendium");
    translations.add("screen.arcane-artistry.guide_book.locked", "Locked");
    translations.add("screen.arcane-artistry.guide_book.unread", "Unread");
    translations.add("screen.arcane-artistry.guide_book.missing_recipe", "Unknown recipe: %s");
    GuideBookBootstrap.addTranslations(translations, bookContent);

    translations.add("options.arcane-artistry.header", "Arcane Artistry");
    translations.add("options.arcane-artistry.mana_bar", "Mana Bar");
    translations.add("options.arcane-artistry.mana_bar.crosshair", "Crosshair");
    translations.add("options.arcane-artistry.mana_bar.bottom_right", "Bottom Right");
    translations.add("options.arcane-artistry.stroke_threshold", "Stroke Threshold");
    translations
        .add("options.arcane-artistry.stroke_threshold.tooltip",
            "How far the mouse must move while casting before a stroke is registered. Lower is more sensitive.");
    translations.add("options.arcane-artistry.max_staff_movement", "Max Staff Movement");
    translations
        .add("options.arcane-artistry.max_staff_movement.tooltip", "How far the staff in first person can move from its center while casting.");
  }

  @Override
  public String getName() {
    return "Arcane Artistry English Translations";
  }
}
