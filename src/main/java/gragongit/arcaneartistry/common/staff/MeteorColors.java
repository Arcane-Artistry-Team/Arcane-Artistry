package gragongit.arcaneartistry.common.staff;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record MeteorColors(List<Integer> head, List<Integer> flame) {
  public static final int MIN_COLORS = 2;
  public static final int MAX_COLORS = 8;

  public MeteorColors {
    head = List.copyOf(head);
    flame = List.copyOf(flame);
  }

  public static final Codec<MeteorColors> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(ExtraCodecs.STRING_RGB_COLOR.listOf(MIN_COLORS, MAX_COLORS).fieldOf("head").forGetter(MeteorColors::head),
              ExtraCodecs.STRING_RGB_COLOR.listOf(MIN_COLORS, MAX_COLORS).fieldOf("flame").forGetter(MeteorColors::flame))
          .apply(instance, MeteorColors::new));
}
