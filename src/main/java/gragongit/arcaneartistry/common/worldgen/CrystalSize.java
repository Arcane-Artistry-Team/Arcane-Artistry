package gragongit.arcaneartistry.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;

/**
 * The size of a crystal: {@code thickness} blocks across and {@code length} long, in blocks or steps depending on the crystal.
 */
public record CrystalSize(int thickness, IntProvider length) {
  public static final Codec<CrystalSize> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(Codec.intRange(1, 8).fieldOf("thickness").forGetter(CrystalSize::thickness),
              IntProviders.codec(1, 32).fieldOf("length").forGetter(CrystalSize::length))
          .apply(instance, CrystalSize::new));
}
