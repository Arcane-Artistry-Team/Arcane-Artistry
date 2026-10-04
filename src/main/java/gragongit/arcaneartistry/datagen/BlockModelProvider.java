package gragongit.arcaneartistry.datagen;

import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.world.item.Items;

public class BlockModelProvider extends FabricModelProvider {

  public BlockModelProvider(FabricPackOutput output) {
    super(output);
  }

  @Override
  public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
    blockModelGenerators.createNonTemplateModelBlock(ModBlocks.ORB_RING);
  }

  @Override
  public void generateItemModels(ItemModelGenerators itemModelGenerators) {
    itemModelGenerators.generateFlatItem(ModItems.LAPIS_CRYSTAL, Items.HEART_OF_THE_SEA, ModelTemplates.FLAT_ITEM);
  }

  @Override
  public String getName() {
    return "Arcane Artistry Block Models";
  }
}
