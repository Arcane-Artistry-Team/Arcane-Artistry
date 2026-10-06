package gragongit.arcaneartistry.datagen;

import java.util.concurrent.CompletableFuture;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;

public class GuideBookProvider extends FabricDynamicRegistryProvider {

  public GuideBookProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  protected void configure(HolderLookup.Provider registries, Entries entries) {
    registries.lookupOrThrow(ModRegistries.BOOK_CATEGORY_KEY).listElements().forEach(reference -> entries.add(reference.key(), reference.value()));
    registries.lookupOrThrow(ModRegistries.BOOK_ENTRY_KEY).listElements().forEach(reference -> entries.add(reference.key(), reference.value()));
  }

  @Override
  public String getName() {
    return "Arcane Artistry Guide Book";
  }
}
