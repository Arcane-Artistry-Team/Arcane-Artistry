package gragongit.arcaneartistry.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

/**
 * Giant caverns spread over the biomes that have this carver. The chunks are divided into cells of {@code spacing}² chunks, and in each
 * cell a cavern starts in one chunk picked at random, if that chunk has the carver. With caverns wider than the spacing, neighbouring ones
 * merge, so the biome becomes one large cave. Each cavern is made of {@code blobs} overlapping ellipsoids around a
 * center at height {@code y}, the first and largest one {@code horizontal_radius} blocks wide and {@code vertical_radius} blocks high and
 * the others bulging out of it. The floor of the whole cavern is cut flat at {@code floor_level} times its vertical radius from its center,
 * and noise of strength {@code roughness} eats into the walls so that they are not smooth.
 *
 * <p>Chunks carve the caverns of all start chunks up to 8 chunks away, so a cavern should stay within about 120 blocks of its center.
 */
public record CavernCarver(int spacing, HeightProvider y, FloatProvider horizontalRadius, FloatProvider verticalRadius, IntProvider blobs,
    FloatProvider floorLevel, float roughness) implements WorldCarver {
  public static final MapCodec<CavernCarver> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(Codec.intRange(1, 16).fieldOf("spacing").forGetter(CavernCarver::spacing),
              HeightProvider.CODEC.fieldOf("y").forGetter(CavernCarver::y),
              FloatProviders.codec(4F, 64F).fieldOf("horizontal_radius").forGetter(CavernCarver::horizontalRadius),
              FloatProviders.codec(4F, 64F).fieldOf("vertical_radius").forGetter(CavernCarver::verticalRadius),
              IntProviders.codec(1, 32).fieldOf("blobs").forGetter(CavernCarver::blobs),
              FloatProviders.codec(-1F, 1F).fieldOf("floor_level").forGetter(CavernCarver::floorLevel),
              Codec.floatRange(0F, 1F).fieldOf("roughness").forGetter(CavernCarver::roughness))
          .apply(instance, CavernCarver::new));

  /**
   * The size in blocks of the bumps that the noise eats into the walls.
   */
  private static final double NOISE_SCALE = 12;

  @Override
  public MapCodec<CavernCarver> codec() {
    return CODEC;
  }

  /**
   * Every chunk is a candidate, {@link #carve} picks the one per cell.
   */
  @Override
  public boolean isStartChunk(RandomSource random) {
    return true;
  }

  @Override
  public boolean carve(WorldGenerationContext context, RandomSource random, ChunkPos chunkPos, ChunkPos sourceChunkPos,
      CarverOutput output) {
    if (!isCellStart(sourceChunkPos)) {
      return false;
    }
    double centerX = sourceChunkPos.getBlockX(random.nextInt(16));
    double centerY = y.sample(random, context);
    double centerZ = sourceChunkPos.getBlockZ(random.nextInt(16));
    double cavernRadius = horizontalRadius.sample(random);
    double cavernHeight = verticalRadius.sample(random);
    double floorY = centerY + floorLevel.sample(random) * cavernHeight;
    SimplexNoise noise = new SimplexNoise(random);

    int count = blobs.sample(random);
    for (int i = 0; i < count; i++) {
      double scale = i == 0 ? 1 : 0.4 + random.nextDouble() * 0.4;
      double angle = random.nextDouble() * Math.PI * 2;
      double distance = random.nextDouble() * cavernRadius * (1 - scale);
      double blobX = centerX + Math.cos(angle) * distance;
      double blobY = centerY + (random.nextDouble() - 0.5) * cavernHeight * (1 - scale);
      double blobZ = centerZ + Math.sin(angle) * distance;
      double radius = cavernRadius * scale;
      double height = cavernHeight * (0.6 + 0.4 * scale);
      WorldCarver.carveEllipsoid(chunkPos, blobX, blobY, blobZ, radius, height, output, (xd, yd, zd, worldY) -> {
        if (worldY - 0.5 <= floorY) {
          return true;
        }
        double bump = noise.get((blobX + xd * radius) / NOISE_SCALE, (worldY - 0.5) / NOISE_SCALE, (blobZ + zd * radius) / NOISE_SCALE);
        return xd * xd + yd * yd + zd * zd >= 1 - roughness * Mth.clamp(0.5 + 0.5 * bump, 0, 1);
      });
    }
    return true;
  }

  /**
   * Whether {@code chunkPos} is the chunk picked to start the cavern of its cell. The pick depends only on the cell, so that it is the same
   * for every chunk that carves the cavern.
   */
  private boolean isCellStart(ChunkPos chunkPos) {
    int cellX = Math.floorDiv(chunkPos.x(), spacing);
    int cellZ = Math.floorDiv(chunkPos.z(), spacing);
    RandomSource cellRandom = RandomSource.create(Mth.getSeed(cellX, 0, cellZ));
    return chunkPos.x() == cellX * spacing + cellRandom.nextInt(spacing) && chunkPos.z() == cellZ * spacing + cellRandom.nextInt(spacing);
  }
}
