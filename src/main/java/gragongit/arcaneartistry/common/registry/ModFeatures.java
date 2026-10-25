package gragongit.arcaneartistry.common.registry;

import com.mojang.serialization.MapCodec;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.worldgen.CavernCarver;
import gragongit.arcaneartistry.common.worldgen.CrystalBridgeFeature;
import gragongit.arcaneartistry.common.worldgen.CrystalLakeFeature;
import gragongit.arcaneartistry.common.worldgen.CrystalPinFeature;
import gragongit.arcaneartistry.common.worldgen.SlantedCrystalPinFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ModFeatures {
  private ModFeatures() {}

  public static final MapCodec<CrystalPinFeature> CRYSTAL_PIN = feature("crystal_pin", CrystalPinFeature.CODEC);
  public static final MapCodec<SlantedCrystalPinFeature> SLANTED_CRYSTAL_PIN =
      feature("slanted_crystal_pin", SlantedCrystalPinFeature.CODEC);
  public static final MapCodec<CrystalBridgeFeature> CRYSTAL_BRIDGE = feature("crystal_bridge", CrystalBridgeFeature.CODEC);
  public static final MapCodec<CrystalLakeFeature> CRYSTAL_LAKE = feature("crystal_lake", CrystalLakeFeature.CODEC);

  public static final MapCodec<CavernCarver> CAVERN = carver("cavern", CavernCarver.CODEC);

  private static <F extends Feature> MapCodec<F> feature(String name, MapCodec<F> codec) {
    return Registry.register(BuiltInRegistries.FEATURE_TYPE, ArcaneArtistry.id(name), codec);
  }

  private static <C extends WorldCarver> MapCodec<C> carver(String name, MapCodec<C> codec) {
    return Registry.register(BuiltInRegistries.CARVER_TYPE, ArcaneArtistry.id(name), codec);
  }

  public static void register() {}
}
