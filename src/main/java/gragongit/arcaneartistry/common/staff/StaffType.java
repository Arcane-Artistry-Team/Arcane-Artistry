package gragongit.arcaneartistry.common.staff;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record StaffType(Identifier icon, Optional<Holder<SoundEvent>> strokeSound, Optional<Holder<SoundEvent>> failSound,
    Optional<Holder<SoundEvent>> noManaSound, int connectionColor, Optional<Identifier> crystalBallBackground) {

  public static final int DEFAULT_CONNECTION_COLOR = 0xC0C0C0;

  public static Builder builder(Identifier icon) {
    return new Builder(icon);
  }

  public Optional<Holder<SoundEvent>> noManaSoundOrFail() {
    return noManaSound.or(this::failSound);
  }

  public static final Codec<StaffType> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(Identifier.CODEC.fieldOf("icon").forGetter(StaffType::icon),
              SoundEvent.CODEC.optionalFieldOf("stroke_sound").forGetter(StaffType::strokeSound),
              SoundEvent.CODEC.optionalFieldOf("fail_sound").forGetter(StaffType::failSound),
              SoundEvent.CODEC.optionalFieldOf("no_mana_sound").forGetter(StaffType::noManaSound),
              ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("connection_color", DEFAULT_CONNECTION_COLOR).forGetter(StaffType::connectionColor),
              Identifier.CODEC.optionalFieldOf("crystal_ball_background").forGetter(StaffType::crystalBallBackground))
          .apply(instance, StaffType::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, Holder<StaffType>> STREAM_CODEC =
      ByteBufCodecs.holderRegistry(ModRegistries.STAFF_TYPE_KEY);

  public static final class Builder {
    private final Identifier icon;
    private @Nullable Holder<SoundEvent> strokeSound;
    private @Nullable Holder<SoundEvent> failSound;
    private @Nullable Holder<SoundEvent> noManaSound;
    private int connectionColor = DEFAULT_CONNECTION_COLOR;
    private @Nullable Identifier crystalBallBackground;

    private Builder(Identifier icon) {
      this.icon = icon;
    }

    public Builder strokeSound(SoundEvent strokeSound) {
      this.strokeSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(strokeSound);
      return this;
    }

    public Builder failSound(SoundEvent failSound) {
      this.failSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(failSound);
      return this;
    }

    public Builder noManaSound(SoundEvent noManaSound) {
      this.noManaSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(noManaSound);
      return this;
    }

    public Builder connectionColor(int connectionColor) {
      this.connectionColor = connectionColor;
      return this;
    }

    public Builder crystalBallBackground(Identifier crystalBallBackground) {
      this.crystalBallBackground = crystalBallBackground;
      return this;
    }

    public StaffType build() {
      return new StaffType(icon, Optional.ofNullable(strokeSound), Optional.ofNullable(failSound), Optional.ofNullable(noManaSound),
          connectionColor, Optional.ofNullable(crystalBallBackground));
    }
  }
}
