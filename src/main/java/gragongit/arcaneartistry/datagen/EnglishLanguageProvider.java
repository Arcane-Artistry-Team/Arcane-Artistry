package gragongit.arcaneartistry.datagen;

import java.util.concurrent.CompletableFuture;
import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import gragongit.arcaneartistry.common.guidebook.SpellPage;
import gragongit.arcaneartistry.common.guidebook.SpellPageDetails;
import gragongit.arcaneartistry.common.guidebook.StaffTypePage;
import gragongit.arcaneartistry.common.mana.ManaAttributes;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModItems;
import gragongit.arcaneartistry.datagen.guidebook.StaffTypeEntryProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class EnglishLanguageProvider extends FabricLanguageProvider {
  private final LanguageProviderCache guideBook;

  public EnglishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture,
      LanguageProviderCache guideBook) {
    super(output, "en_us", registriesFuture);
    this.guideBook = guideBook;
  }

  @Override
  public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder translations) {
    translations.add(ModBlocks.ORB_RING, "Orb Ring");
    translations.add(ModItems.LAPIS_CRYSTAL, "Lapis Crystal");

    translations.add(ManaAttributes.MAX_MANA, "Max Mana");

    translations.add("screen.arcane-artistry.crystal_ball", "Crystal Ball");
    translations.add("screen.arcane-artistry.crystal_ball.home", "Home");

    guideBook.data().forEach(translations::add);
    translations.add(StaffTypeEntryProvider.LOCKED, "Requires: %s");
    translations.add(StaffTypePage.ITEMS, "Staffs: %s");
    translations.add(StaffTypePage.UNKNOWN, "This staff type no longer exists.");
    translations.add(SpellPageDetails.PATTERN, "Pattern: %s");
    translations.add(SpellPageDetails.MANA_COST, "Mana: %s");
    translations.add(SpellPage.UNKNOWN, "This spell no longer exists.");

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
