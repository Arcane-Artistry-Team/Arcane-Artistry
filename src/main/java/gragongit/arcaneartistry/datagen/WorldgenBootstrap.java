package gragongit.arcaneartistry.datagen;

import java.util.ArrayList;
import java.util.List;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModBiomes;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.worldgen.CavernCarver;
import gragongit.arcaneartistry.common.worldgen.CrystalAttachment;
import gragongit.arcaneartistry.common.worldgen.CrystalBridgeFeature;
import gragongit.arcaneartistry.common.worldgen.CrystalLakeFeature;
import gragongit.arcaneartistry.common.worldgen.CrystalPinFeature;
import gragongit.arcaneartistry.common.worldgen.CrystalSize;
import gragongit.arcaneartistry.common.worldgen.SlantedCrystalPinFeature;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public final class WorldgenBootstrap {
  private WorldgenBootstrap() {}

  public static final ResourceKey<WorldCarver> CRYSTAL_CAVERN = ResourceKey.create(Registries.CARVER, ArcaneArtistry.id("crystal_cavern"));

  public static final ResourceKey<Feature> LAPIS_CRYSTAL_PIN = feature("lapis_crystal_pin");
  public static final ResourceKey<Feature> TILTED_LAPIS_CRYSTAL_PIN = feature("tilted_lapis_crystal_pin");
  public static final ResourceKey<Feature> DIAGONAL_LAPIS_CRYSTAL_PIN = feature("diagonal_lapis_crystal_pin");
  public static final ResourceKey<Feature> LAPIS_CRYSTAL_BRIDGE = feature("lapis_crystal_bridge");
  public static final ResourceKey<Feature> GIANT_LAPIS_CRYSTAL_PIN = feature("giant_lapis_crystal_pin");
  public static final ResourceKey<Feature> GIANT_TILTED_LAPIS_CRYSTAL_PIN = feature("giant_tilted_lapis_crystal_pin");
  public static final ResourceKey<Feature> GIANT_DIAGONAL_LAPIS_CRYSTAL_PIN = feature("giant_diagonal_lapis_crystal_pin");
  public static final ResourceKey<Feature> GIANT_LAPIS_CRYSTAL_BRIDGE = feature("giant_lapis_crystal_bridge");
  public static final ResourceKey<Feature> LAPIS_CRYSTAL_LAKE = feature("lapis_crystal_lake");
  public static final ResourceKey<Feature> SMALL_LAPIS_CRYSTAL_LAKE = feature("small_lapis_crystal_lake");

  public static final ResourceKey<PlacedFeature> LAPIS_CRYSTAL_PIN_PLACED = placedFeature("lapis_crystal_pin");
  public static final ResourceKey<PlacedFeature> TILTED_LAPIS_CRYSTAL_PIN_PLACED = placedFeature("tilted_lapis_crystal_pin");
  public static final ResourceKey<PlacedFeature> DIAGONAL_LAPIS_CRYSTAL_PIN_PLACED = placedFeature("diagonal_lapis_crystal_pin");
  public static final ResourceKey<PlacedFeature> LAPIS_CRYSTAL_BRIDGE_PLACED = placedFeature("lapis_crystal_bridge");
  public static final ResourceKey<PlacedFeature> GIANT_LAPIS_CRYSTAL_PIN_PLACED = placedFeature("giant_lapis_crystal_pin");
  public static final ResourceKey<PlacedFeature> GIANT_TILTED_LAPIS_CRYSTAL_PIN_PLACED = placedFeature("giant_tilted_lapis_crystal_pin");
  public static final ResourceKey<PlacedFeature> GIANT_DIAGONAL_LAPIS_CRYSTAL_PIN_PLACED =
      placedFeature("giant_diagonal_lapis_crystal_pin");
  public static final ResourceKey<PlacedFeature> GIANT_LAPIS_CRYSTAL_BRIDGE_PLACED = placedFeature("giant_lapis_crystal_bridge");
  public static final ResourceKey<PlacedFeature> LAPIS_CRYSTAL_LAKE_PLACED = placedFeature("lapis_crystal_lake");
  public static final ResourceKey<PlacedFeature> SMALL_LAPIS_CRYSTAL_LAKE_PLACED = placedFeature("small_lapis_crystal_lake");

  private static ResourceKey<Feature> feature(String name) {
    return ResourceKey.create(Registries.FEATURE, ArcaneArtistry.id(name));
  }

  private static ResourceKey<PlacedFeature> placedFeature(String name) {
    return ResourceKey.create(Registries.PLACED_FEATURE, ArcaneArtistry.id(name));
  }

  static void bootstrapCarvers(BootstrapContext<WorldCarver> context) {
    // One cavern every 6 chunks, about 100 blocks wide, so that the caverns merge into one large cave. They lie entirely within the
    // deepslate below Y 0: the top of a cavern reaches at most about 1.06 times its vertical radius above its center, so at most to
    // Y -10, and its floor stays above the lava at Y -54.
    context
        .register(CRYSTAL_CAVERN, new CavernCarver(6, UniformHeight.of(VerticalAnchor.absolute(-36), VerticalAnchor.absolute(-30)),
            UniformFloat.of(32F, 56F), UniformFloat.of(12F, 18F), UniformInt.of(5, 9), UniformFloat.of(-0.6F, -0.4F), 0.3F));
  }

  static void bootstrapFeatures(BootstrapContext<Feature> context) {
    HolderSet<Block> replaceable = context.lookup(Registries.BLOCK).getOrThrow(BlockTags.LUSH_GROUND_REPLACEABLE);
    Holder<BlockStateProvider> lapisCrystal = BlockStateProvider.holderOf(ModBlocks.LAPIS_CRYSTAL);

    List<CrystalAttachment> anySurface = List.of(CrystalAttachment.WALL, CrystalAttachment.FLOOR, CrystalAttachment.CEILING);
    // Many thin towers and few thick ones.
    context
        .register(LAPIS_CRYSTAL_PIN, new CrystalPinFeature(lapisCrystal, WeightedList
            .<CrystalSize>builder()
            .add(new CrystalSize(1, UniformInt.of(1, 3)), 6)
            .add(new CrystalSize(2, UniformInt.of(2, 4)), 3)
            .add(new CrystalSize(3, UniformInt.of(3, 9)), 1)
            .build(), anySurface, 8));
    WeightedList<CrystalSize> slantedSizes = WeightedList
        .<CrystalSize>builder()
        .add(new CrystalSize(2, UniformInt.of(4, 7)), 2)
        .add(new CrystalSize(3, UniformInt.of(5, 10)), 1)
        .build();
    context
        .register(TILTED_LAPIS_CRYSTAL_PIN, new SlantedCrystalPinFeature(lapisCrystal, SlantedCrystalPinFeature.Slant.EDGE, slantedSizes,
            UniformInt.of(1, 4), UniformInt.of(1, 2), 8));
    context
        .register(DIAGONAL_LAPIS_CRYSTAL_PIN, new SlantedCrystalPinFeature(lapisCrystal, SlantedCrystalPinFeature.Slant.CORNER,
            slantedSizes, UniformInt.of(1, 4), UniformInt.of(1, 2), 8));
    context
        .register(LAPIS_CRYSTAL_BRIDGE, new CrystalBridgeFeature(lapisCrystal, List.of(Direction.Axis.values()), 1, 23, List.of(8, 15),
            UniformFloat.of(0F, 1F), UniformInt.of(0, 3), UniformInt.of(1, 3)));

    // Giant versions, four blocks thick and the longest.
    context
        .register(GIANT_LAPIS_CRYSTAL_PIN,
            new CrystalPinFeature(lapisCrystal, WeightedList.of(new CrystalSize(4, UniformInt.of(4, 12))), anySurface, 12));
    WeightedList<CrystalSize> giantSlantedSizes = WeightedList.of(new CrystalSize(4, UniformInt.of(8, 14)));
    context
        .register(GIANT_TILTED_LAPIS_CRYSTAL_PIN, new SlantedCrystalPinFeature(lapisCrystal, SlantedCrystalPinFeature.Slant.EDGE,
            giantSlantedSizes, UniformInt.of(2, 6), UniformInt.of(1, 3), 12));
    context
        .register(GIANT_DIAGONAL_LAPIS_CRYSTAL_PIN, new SlantedCrystalPinFeature(lapisCrystal, SlantedCrystalPinFeature.Slant.CORNER,
            giantSlantedSizes, UniformInt.of(2, 6), UniformInt.of(1, 3), 12));
    context
        .register(GIANT_LAPIS_CRYSTAL_BRIDGE, new CrystalBridgeFeature(lapisCrystal, List.of(Direction.Axis.values()), 12, 32,
            List.of(1, 1, 1), UniformFloat.of(0F, 1F), UniformInt.of(2, 5), UniformInt.of(1, 4)));

    context
        .register(LAPIS_CRYSTAL_LAKE, new CrystalLakeFeature(lapisCrystal, UniformInt.of(5, 10), UniformInt.of(2, 3), UniformInt.of(2, 3),
            ConstantInt.of(2), replaceable));
    context
        .register(SMALL_LAPIS_CRYSTAL_LAKE, new CrystalLakeFeature(lapisCrystal, UniformInt.of(2, 4), ConstantInt.of(1), ConstantInt.of(1),
            ConstantInt.of(1), replaceable));
  }

  static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
    HolderGetter<Feature> features = context.lookup(Registries.FEATURE);
    // Most positions lie in solid rock and are dropped, so the counts are attempts rather than crystals. They are limited to the heights
    // of the underground biome so that fewer of them are wasted.
    HeightRangePlacement caves = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64));
    List<PlacementModifier> inCaveAir = List.of(InSquarePlacement.spread(), caves,
        BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE), BiomeFilter.biome());
    // Lakes sit on the floor, so search downwards from the random position for it.
    List<PlacementModifier> onCaveFloor = List.of(InSquarePlacement.spread(), caves,
        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
        OffsetPlacement.vertical(ConstantInt.of(1)), BiomeFilter.biome());

    register(context, GIANT_LAPIS_CRYSTAL_PIN_PLACED, features.getOrThrow(GIANT_LAPIS_CRYSTAL_PIN), 6, inCaveAir);
    register(context, GIANT_TILTED_LAPIS_CRYSTAL_PIN_PLACED, features.getOrThrow(GIANT_TILTED_LAPIS_CRYSTAL_PIN), 4, inCaveAir);
    register(context, GIANT_DIAGONAL_LAPIS_CRYSTAL_PIN_PLACED, features.getOrThrow(GIANT_DIAGONAL_LAPIS_CRYSTAL_PIN), 3, inCaveAir);
    register(context, GIANT_LAPIS_CRYSTAL_BRIDGE_PLACED, features.getOrThrow(GIANT_LAPIS_CRYSTAL_BRIDGE), 4, inCaveAir);
    register(context, LAPIS_CRYSTAL_PIN_PLACED, features.getOrThrow(LAPIS_CRYSTAL_PIN), 96, inCaveAir);
    register(context, TILTED_LAPIS_CRYSTAL_PIN_PLACED, features.getOrThrow(TILTED_LAPIS_CRYSTAL_PIN), 24, inCaveAir);
    register(context, DIAGONAL_LAPIS_CRYSTAL_PIN_PLACED, features.getOrThrow(DIAGONAL_LAPIS_CRYSTAL_PIN), 16, inCaveAir);
    register(context, LAPIS_CRYSTAL_BRIDGE_PLACED, features.getOrThrow(LAPIS_CRYSTAL_BRIDGE), 8, inCaveAir);
    register(context, LAPIS_CRYSTAL_LAKE_PLACED, features.getOrThrow(LAPIS_CRYSTAL_LAKE), 6, onCaveFloor);
    register(context, SMALL_LAPIS_CRYSTAL_LAKE_PLACED, features.getOrThrow(SMALL_LAPIS_CRYSTAL_LAKE), 6, onCaveFloor);
  }

  /**
   * Registers {@code feature} with {@code count} attempts per chunk placed by {@code placement}.
   */
  private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<Feature> feature, int count,
      List<PlacementModifier> placement) {
    List<PlacementModifier> modifiers = new ArrayList<>();
    modifiers.add(CountPlacement.of(count));
    modifiers.addAll(placement);
    PlacementUtils.register(context, key, feature, modifiers);
  }

  static void bootstrapBiomes(BootstrapContext<Biome> context) {
    HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
    HolderGetter<WorldCarver> carvers = context.lookup(Registries.CARVER);

    MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
    BiomeDefaultFeatures.commonSpawns(mobs);
    BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
    OverworldBiomes.globalOverworldGeneration(generation);
    BiomeDefaultFeatures.addDefaultOres(generation);
    BiomeDefaultFeatures.addDefaultSoftDisks(generation);
    generation.addCarver(CRYSTAL_CAVERN);
    generation.addFeature(GenerationStep.Decoration.LAKES, LAPIS_CRYSTAL_LAKE_PLACED);
    generation.addFeature(GenerationStep.Decoration.LAKES, SMALL_LAPIS_CRYSTAL_LAKE_PLACED);
    // The giant crystals come first, since crystals are skipped where they would touch others.
    generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, GIANT_LAPIS_CRYSTAL_BRIDGE_PLACED);
    generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, GIANT_TILTED_LAPIS_CRYSTAL_PIN_PLACED);
    generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, GIANT_DIAGONAL_LAPIS_CRYSTAL_PIN_PLACED);
    generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, GIANT_LAPIS_CRYSTAL_PIN_PLACED);
    generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, LAPIS_CRYSTAL_BRIDGE_PLACED);
    generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, TILTED_LAPIS_CRYSTAL_PIN_PLACED);
    generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, DIAGONAL_LAPIS_CRYSTAL_PIN_PLACED);
    generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, LAPIS_CRYSTAL_PIN_PLACED);

    context
        .register(ModBiomes.CRYSTAL_CAVES,
            OverworldBiomes.baseBiome(0.5F, 0.5F).mobSpawnSettings(mobs.build()).generationSettings(generation.build()).build());
  }
}
