package gragongit.arcaneartistry.common.staff;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;

public record StaffType(Optional<Holder<SoundEvent>> strokeSound, Optional<Holder<SoundEvent>> failSound) {

  public StaffType(SoundEvent strokeSound, SoundEvent failSound) {
    this(Optional.ofNullable(strokeSound).map(BuiltInRegistries.SOUND_EVENT::wrapAsHolder),
        Optional.ofNullable(failSound).map(BuiltInRegistries.SOUND_EVENT::wrapAsHolder));
  }

  public static final Codec<StaffType> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(SoundEvent.CODEC.optionalFieldOf("stroke_sound").forGetter(StaffType::strokeSound),
              SoundEvent.CODEC.optionalFieldOf("fail_sound").forGetter(StaffType::failSound))
          .apply(instance, StaffType::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, Holder<StaffType>> STREAM_CODEC =
      ByteBufCodecs.holderRegistry(ModRegistries.STAFF_TYPE_KEY);
}
