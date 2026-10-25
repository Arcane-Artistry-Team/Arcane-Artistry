package gragongit.arcaneartistry.common.registry;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomes {
  private ModBiomes() {}

  public static final ResourceKey<Biome> CRYSTAL_CAVES = ResourceKey.create(Registries.BIOME, ArcaneArtistry.id("crystal_caves"));
}
