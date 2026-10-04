package gragongit.arcaneartistry.datagen;

import java.util.concurrent.CompletableFuture;
import gragongit.arcaneartistry.common.block.OrbRingBlock;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class BlockLootProvider extends FabricBlockLootSubProvider {

  public BlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  public void generate() {
    add(ModBlocks.ORB_RING,
        createSingleItemTable(ModBlocks.ORB_RING)
            .withPool(applyExplosionCondition(ModItems.LAPIS_CRYSTAL,
                LootPool
                    .lootPool()
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem
                        .lootTableItem(ModItems.LAPIS_CRYSTAL)
                        .when(MatchBlock
                            .blockMatches(blocks, ModBlocks.ORB_RING,
                                StatePropertiesPredicate.Builder.properties().hasProperty(OrbRingBlock.CRYSTAL, true)))))));
  }

  @Override
  public String getName() {
    return "Arcane Artistry Block Loot Tables";
  }
}
