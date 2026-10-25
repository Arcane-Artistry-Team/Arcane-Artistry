package gragongit.arcaneartistry.common.worldgen;

import java.util.function.Consumer;
import com.mojang.datafixers.util.Pair;
import gragongit.arcaneartistry.common.registry.ModBiomes;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;

/**
 * A TerraBlender region of the overworld that keeps the vanilla biomes and adds the crystal caves underground. They are chosen by
 * weirdness rather than by climate so that they appear below every kind of surface, and leave out the climates of the lush, dripstone and
 * sulfur caves so that no two cave biomes compete for the same place.
 */
public class CrystalCavesRegion extends Region {

  public CrystalCavesRegion(Identifier name, int weight) {
    super(name, RegionType.OVERWORLD, weight);
  }

  @Override
  public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
    addModifiedVanillaOverworldBiomes(mapper, builder -> {
    });
    addBiome(mapper, ParameterUtils.Temperature.FULL_RANGE.parameter(), Climate.Parameter.span(-1.0F, 0.7F),
        Climate.Parameter.span(-1.2F, 0.8F), ParameterUtils.Erosion.FULL_RANGE.parameter(), Climate.Parameter.span(-0.85F, 0.0F),
        ParameterUtils.Depth.UNDERGROUND.parameter(), 0.0F, ModBiomes.CRYSTAL_CAVES);
  }
}
