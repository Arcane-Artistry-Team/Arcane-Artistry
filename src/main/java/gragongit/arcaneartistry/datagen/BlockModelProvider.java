package gragongit.arcaneartistry.datagen;

import java.util.Map;
import java.util.Optional;
import gragongit.arcaneartistry.client.model.crystal.CrystalBlockModel;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class BlockModelProvider extends FabricModelProvider {

  public BlockModelProvider(FabricPackOutput output) {
    super(output);
  }

  @Override
  public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
    blockModelGenerators.createNonTemplateModelBlock(ModBlocks.ORB_RING);
    blockModelGenerators.blockStateOutput.accept(crystal(ModBlocks.LAPIS_CRYSTAL, ArcaneArtistry.id("block/lapis_crystal")));
  }

  @Override
  public void generateItemModels(ItemModelGenerators itemModelGenerators) {
    itemModelGenerators.generateFlatItem(ModItems.LAPIS_CRYSTAL, Items.HEART_OF_THE_SEA, ModelTemplates.FLAT_ITEM);
  }

  private static BlockModelDefinitionGenerator crystal(Block block, Identifier texture) {
    // The custom model type has to be known to serialize the block state definition.
    CrystalBlockModel.register();
    return new BlockModelDefinitionGenerator() {
      @Override
      public Block block() {
        return block;
      }

      @Override
      public BlockStateModelDispatcher create() {
        return new BlockStateModelDispatcher(
            Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(Map.of("", new CrystalBlockModel.Unbaked(texture)))),
            Optional.empty());
      }
    };
  }

  @Override
  public String getName() {
    return "Arcane Artistry Block Models";
  }
}
