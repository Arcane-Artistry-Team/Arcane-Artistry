package gragongit.arcaneartistry.common.registry;

import java.util.function.Function;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.block.OrbRingBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
  private ModBlocks() {}

  public static final BlockItemId ORB_RING_ID = id("orb_ring");

  public static final Block ORB_RING = register(ORB_RING_ID, OrbRingBlock::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).sound(SoundType.METAL).noOcclusion());

  private static BlockItemId id(String name) {
    Identifier id = ArcaneArtistry.id(name);
    return BlockItemId.create(id, id);
  }

  private static Block register(BlockItemId id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
    Block block = Registry.register(BuiltInRegistries.BLOCK, id.block(), factory.apply(properties.setId(id.block())));
    Registry.register(BuiltInRegistries.ITEM, id.item(), new BlockItem(block, new Item.Properties().setId(id.item()).useBlockDescriptionPrefix()));

    return block;
  }

  public static void register() {}
}
