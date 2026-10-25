package gragongit.arcaneartistry.datagen;

import java.util.List;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

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
    pack.addProvider(WorldgenProvider::new);

    modules.forEach(module -> module.addProviders(pack));
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    registryBuilder.add(Registries.CARVER, WorldgenBootstrap::bootstrapCarvers);
    registryBuilder.add(Registries.FEATURE, WorldgenBootstrap::bootstrapFeatures);
    registryBuilder.add(Registries.PLACED_FEATURE, WorldgenBootstrap::bootstrapPlacedFeatures);
    registryBuilder.add(Registries.BIOME, WorldgenBootstrap::bootstrapBiomes);
    modules.forEach(module -> module.buildRegistry(registryBuilder));
  }
}
