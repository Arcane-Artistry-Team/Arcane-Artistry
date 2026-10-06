package gragongit.arcaneartistry.datagen;

import java.util.List;
import java.util.stream.Stream;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.datagen.guidebook.BookContent;
import gragongit.arcaneartistry.datagen.guidebook.CoreBookContent;
import gragongit.arcaneartistry.datagen.guidebook.GuideBookBootstrap;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistrySetBuilder;

public class ArcaneArtistryDataGenerator implements DataGeneratorEntrypoint {
  private final List<ArcaneArtistryDatagenModule> modules =
      FabricLoader.getInstance().getEntrypoints(ArcaneArtistryDatagenModule.ENTRYPOINT_KEY, ArcaneArtistryDatagenModule.class);

  private final List<BookContent> bookContent =
      Stream.concat(Stream.of(new CoreBookContent()), modules.stream().flatMap(module -> module.bookContent().stream())).toList();

  @Override
  public void onInitializeDataGenerator(FabricDataGenerator generator) {
    FabricDataGenerator.Pack pack = generator.createPack();
    pack.addProvider(BlockModelProvider::new);
    pack.addProvider(BlockLootProvider::new);
    pack.addProvider(BlockTagProvider::new);
    pack.addProvider((output, registries) -> new EnglishLanguageProvider(output, registries, bookContent));
    pack.addProvider(StaffTypeProvider::new);
    pack.addProvider(SpellProvider::new);
    pack.addProvider(GuideBookProvider::new);

    modules.forEach(module -> module.addProviders(pack));
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    registryBuilder.add(ModRegistries.BOOK_CATEGORY_KEY, context -> GuideBookBootstrap.bootstrapCategories(context, bookContent));
    registryBuilder.add(ModRegistries.BOOK_ENTRY_KEY, context -> GuideBookBootstrap.bootstrapEntries(context, bookContent));
    modules.forEach(module -> module.buildRegistry(registryBuilder));
  }
}
