package gragongit.arcaneartistry.datagen;

import java.util.List;
import com.klikli_dev.modonomicon.api.datagen.FabricBookProvider;
import com.klikli_dev.modonomicon.api.datagen.FabricResearchProvider;
import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import com.klikli_dev.modonomicon.api.datagen.research.ResearchCache;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.datagen.guidebook.GuideBookProvider;
import gragongit.arcaneartistry.datagen.guidebook.GuideBookResearch;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistrySetBuilder;

public class ArcaneArtistryDataGenerator implements DataGeneratorEntrypoint {
  private final List<ArcaneArtistryDatagenModule> modules =
      FabricLoader.getInstance().getEntrypoints(ArcaneArtistryDatagenModule.ENTRYPOINT_KEY, ArcaneArtistryDatagenModule.class);

  @Override
  public void onInitializeDataGenerator(FabricDataGenerator generator) {
    FabricDataGenerator.Pack pack = generator.createPack();
    pack.addProvider(BlockModelProvider::new);
    pack.addProvider(BlockLootProvider::new);
    pack.addProvider(BlockTagProvider::new);
    pack.addProvider(StaffTypeProvider::new);
    pack.addProvider(SpellProvider::new);

    LanguageProviderCache guideBookLang = new LanguageProviderCache("en_us");
    ResearchCache guideBookResearch = new ResearchCache();
    pack.addProvider(FabricBookProvider.of(ArcaneArtistry.MOD_ID, guideBookLang, guideBookResearch, new GuideBookProvider()));
    modules.forEach(module -> module.addGuideBookProviders(pack, guideBookLang));
    pack.addProvider(FabricResearchProvider.of(ArcaneArtistry.MOD_ID, guideBookLang, guideBookResearch, new GuideBookResearch()));
    pack.addProvider((output, registries) -> new EnglishLanguageProvider(output, registries, guideBookLang));

    modules.forEach(module -> module.addProviders(pack));
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    modules.forEach(module -> module.buildRegistry(registryBuilder));
  }
}
