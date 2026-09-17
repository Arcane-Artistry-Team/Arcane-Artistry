package gragongit.arcaneartistry.datagen;

import java.util.concurrent.CompletableFuture;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;

public class SpellProvider extends FabricDynamicRegistryProvider {

  public SpellProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  protected void configure(HolderLookup.Provider registries, Entries entries) {
    registries.lookupOrThrow(ModRegistries.SPELL_KEY).listElements().forEach(reference -> entries.add(reference.key(), reference.value()));;
  }

  @Override
  public String getName() {
    return "Arcane Artistry Spells";
  }
}
