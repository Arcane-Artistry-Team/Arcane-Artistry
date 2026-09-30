package gragongit.arcaneartistry.common.crystalball;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record CrystalBallEntry(Identifier icon, Component title, Optional<Component> description) {
  public static final MapCodec<CrystalBallEntry> MAP_CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(Identifier.CODEC.fieldOf("icon").forGetter(CrystalBallEntry::icon),
              ComponentSerialization.CODEC.fieldOf("title").forGetter(CrystalBallEntry::title),
              ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(CrystalBallEntry::description))
          .apply(instance, CrystalBallEntry::new));

  public static final Codec<CrystalBallEntry> CODEC = MAP_CODEC.codec();
}
