package gragongit.arcaneartistry.datagen;

import java.util.List;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.datagen.guidebook.BookContent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public interface ArcaneArtistryDatagenModule {
  String ENTRYPOINT_KEY = ArcaneArtistry.MOD_ID + ":datagen";

  default void addProviders(FabricDataGenerator.Pack pack) {}

  default void buildRegistry(RegistrySetBuilder registryBuilder) {}

  /** Guide book categories and entries of this module. Their translations end up in the core language file. */
  default List<BookContent> bookContent() {
    return List.of();
  }
}
