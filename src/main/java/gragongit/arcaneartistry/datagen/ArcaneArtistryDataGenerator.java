package gragongit.arcaneartistry.datagen;

import java.util.List;
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
    pack.addProvider(EnglishLanguageProvider::new);
    pack.addProvider(StaffTypeProvider::new);
    pack.addProvider(SpellProvider::new);

    modules.forEach(module -> module.addProviders(pack));
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    modules.forEach(module -> module.buildRegistry(registryBuilder));
  }
}
