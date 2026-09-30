package gragongit.arcaneartistry.common.crystalball;

import java.util.Optional;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record CrystalBallTheme(int connectionColor, Optional<Identifier> background) {
  public static final int DEFAULT_CONNECTION_COLOR = 0xC0C0C0;

  public static final MapCodec<CrystalBallTheme> MAP_CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(
              ExtraCodecs.STRING_RGB_COLOR
                  .optionalFieldOf("connection_color", DEFAULT_CONNECTION_COLOR)
                  .forGetter(CrystalBallTheme::connectionColor),
              Identifier.CODEC.optionalFieldOf("background").forGetter(CrystalBallTheme::background))
          .apply(instance, CrystalBallTheme::new));
}
