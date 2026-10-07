package gragongit.arcaneartistry.elements.datagen;

import com.klikli_dev.modonomicon.api.datagen.FabricBookProvider;
import com.klikli_dev.modonomicon.api.datagen.FabricResearchProvider;
import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import com.klikli_dev.modonomicon.api.datagen.research.ResearchCache;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.datagen.ArcaneArtistryDatagenModule;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import gragongit.arcaneartistry.elements.datagen.guidebook.ElementsGuideBook;
import gragongit.arcaneartistry.elements.datagen.guidebook.ElementsResearch;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public class ArcaneArtistryElementsDataGenerator implements ArcaneArtistryDatagenModule {

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    registryBuilder.add(ModRegistries.STAFF_TYPE_KEY, StaffTypeBootstrap::bootstrapStaffTypes);
    registryBuilder.add(ModRegistries.SPELL_KEY, SpellBootstrap::bootstrapSpells);
  }

  @Override
  public void addGuideBookProviders(FabricDataGenerator.Pack pack, LanguageProviderCache lang) {
    ResearchCache research = new ResearchCache();
    pack.addProvider(FabricBookProvider.of(ArcaneArtistryElements.MOD_ID, lang, research, new ElementsGuideBook()));
    pack.addProvider(FabricResearchProvider.of(ArcaneArtistryElements.MOD_ID, lang, research, new ElementsResearch()));
  }
}
