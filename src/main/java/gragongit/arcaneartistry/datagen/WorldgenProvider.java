package gragongit.arcaneartistry.datagen;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

public class WorldgenProvider extends FabricDynamicRegistryProvider {

  public WorldgenProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  protected void configure(HolderLookup.Provider registries, Entries entries) {
    entries.addAll(registries.lookupOrThrow(Registries.CARVER));
    entries.addAll(registries.lookupOrThrow(Registries.FEATURE));
    entries.addAll(registries.lookupOrThrow(Registries.PLACED_FEATURE));
    entries.addAll(registries.lookupOrThrow(Registries.BIOME));
  }

  @Override
  public String getName() {
    return "Arcane Artistry Worldgen";
  }
}
