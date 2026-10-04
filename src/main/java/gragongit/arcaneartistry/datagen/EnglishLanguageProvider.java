package gragongit.arcaneartistry.datagen;

import java.util.concurrent.CompletableFuture;
import gragongit.arcaneartistry.common.mana.ManaAttributes;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class EnglishLanguageProvider extends FabricLanguageProvider {

  public EnglishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, "en_us", registriesFuture);
  }

  @Override
  public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder translations) {
    translations.add(ModBlocks.ORB_RING, "Orb Ring");
    translations.add(ModItems.LAPIS_CRYSTAL, "Lapis Crystal");

    translations.add(ManaAttributes.MAX_MANA, "Max Mana");

    translations.add("screen.arcane-artistry.crystal_ball", "Crystal Ball");
    translations.add("screen.arcane-artistry.crystal_ball.home", "Home");

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
