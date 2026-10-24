package gragongit.arcaneartistry.datagen;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
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
    blockModelGenerators.blockStateOutput.accept(crystal(ModBlocks.LAPIS_CRYSTAL, "lapis_crystal", 3, 16));
  }

  @Override
  public void generateItemModels(ItemModelGenerators itemModelGenerators) {
    itemModelGenerators.generateFlatItem(ModItems.LAPIS_CRYSTAL, Items.HEART_OF_THE_SEA, ModelTemplates.FLAT_ITEM);
  }

  /**
   * The model of a crystal using the texture {@code block/<name>}, the texture {@code block/<name>_inner} for its interior and the shimmer
   * textures {@code block/<name>_shimmer_<1..variants>}. Each side shimmers with a chance of one in {@code shimmerChance}.
   */
  private static BlockModelDefinitionGenerator crystal(Block block, String name, int shimmerVariants, int shimmerChance) {
    Identifier texture = ArcaneArtistry.id("block/" + name);
    Identifier innerTexture = ArcaneArtistry.id("block/" + name + "_inner");
    List<Identifier> shimmerTextures =
        IntStream.rangeClosed(1, shimmerVariants).mapToObj(i -> ArcaneArtistry.id("block/" + name + "_shimmer_" + i)).toList();
    // The custom model type has to be known to serialize the block state definition.
    CrystalBlockModel.register();
    return new BlockModelDefinitionGenerator() {
      @Override
      public Block block() {
        return block;
      }

      @Override
      public BlockStateModelDispatcher create() {
        return new BlockStateModelDispatcher(Optional
            .of(new BlockStateModelDispatcher.SimpleModelSelectors(
                Map.of("", new CrystalBlockModel.Unbaked(texture, Optional.of(innerTexture), shimmerTextures, shimmerChance)))),
            Optional.empty());
      }
    };
  }

  @Override
  public String getName() {
    return "Arcane Artistry Block Models";
  }
}
