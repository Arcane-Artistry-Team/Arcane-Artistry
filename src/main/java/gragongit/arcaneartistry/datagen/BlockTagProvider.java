package gragongit.arcaneartistry.datagen;

import java.util.concurrent.CompletableFuture;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

public class BlockTagProvider extends FabricTagsProvider.BlockTagsProvider {

  public BlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {
    builder(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.ORB_RING_ID);
  }

  @Override
  public String getName() {
    return "Arcane Artistry Block Tags";
  }
}
