package gragongit.arcaneartistry.datagen;

import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public interface ArcaneArtistryDatagenModule {
  String ENTRYPOINT_KEY = ArcaneArtistry.MOD_ID + ":datagen";

  default void addProviders(FabricDataGenerator.Pack pack) {}

  default void buildRegistry(RegistrySetBuilder registryBuilder) {}

  default void addGuideBookProviders(FabricDataGenerator.Pack pack, LanguageProviderCache lang) {}
}
