package gragongit.arcaneartistry.common.presentation;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record Presentation(Identifier icon, Component title, Optional<Component> description) {
  public static final MapCodec<Presentation> MAP_CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(Identifier.CODEC.fieldOf("icon").forGetter(Presentation::icon),
              ComponentSerialization.CODEC.fieldOf("title").forGetter(Presentation::title),
              ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(Presentation::description))
          .apply(instance, Presentation::new));

  public static final Codec<Presentation> CODEC = MAP_CODEC.codec();
}
