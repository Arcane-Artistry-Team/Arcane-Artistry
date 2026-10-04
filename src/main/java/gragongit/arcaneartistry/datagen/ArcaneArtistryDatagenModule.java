package gragongit.arcaneartistry.datagen;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public interface ArcaneArtistryDatagenModule {
  String ENTRYPOINT_KEY = ArcaneArtistry.MOD_ID + ":datagen";

  default void addProviders(FabricDataGenerator.Pack pack) {}

  default void buildRegistry(RegistrySetBuilder registryBuilder) {}
}
