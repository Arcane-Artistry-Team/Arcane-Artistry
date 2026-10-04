package gragongit.arcaneartistry.common.registry;

import java.util.Set;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.block.OrbRingBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
  private ModBlockEntities() {}

  public static final BlockEntityType<OrbRingBlockEntity> ORB_RING = Registry
      .register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ArcaneArtistry.id("orb_ring"),
          new BlockEntityType<>(OrbRingBlockEntity::new, Set.of(ModBlocks.ORB_RING)));

  public static void register() {}
}
