package gragongit.arcaneartistry.datagen;

import gragongit.arcaneartistry.common.registry.ModBlocks;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;

public class BlockModelProvider extends FabricModelProvider {

  public BlockModelProvider(FabricPackOutput output) {
    super(output);
  }

  @Override
  public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
    blockModelGenerators.createNonTemplateModelBlock(ModBlocks.ORB_RING);
  }

  @Override
  public void generateItemModels(ItemModelGenerators itemModelGenerators) {}

  @Override
  public String getName() {
    return "Arcane Artistry Block Models";
  }
}
