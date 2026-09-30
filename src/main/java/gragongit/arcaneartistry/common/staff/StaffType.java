package gragongit.arcaneartistry.common.staff;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.crystalball.CrystalBallEntry;
import gragongit.arcaneartistry.common.crystalball.CrystalBallTheme;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public record StaffType(Optional<Holder<SoundEvent>> strokeSound, Optional<Holder<SoundEvent>> failSound,
    Optional<Holder<SoundEvent>> noManaSound, CrystalBallTheme crystalBallTheme, CrystalBallEntry crystalBallEntry) {

  public static Builder builder() {
    return new Builder();
  }

  public Optional<Holder<SoundEvent>> noManaSoundOrFail() {
    return noManaSound.or(this::failSound);
  }

  private static final Codec<Pair<CrystalBallTheme, CrystalBallEntry>> CRYSTAL_BALL_CODEC =
      Codec.mapPair(CrystalBallTheme.MAP_CODEC, CrystalBallEntry.MAP_CODEC).codec();

  public static final Codec<StaffType> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(SoundEvent.CODEC.optionalFieldOf("stroke_sound").forGetter(StaffType::strokeSound),
              SoundEvent.CODEC.optionalFieldOf("fail_sound").forGetter(StaffType::failSound),
              SoundEvent.CODEC.optionalFieldOf("no_mana_sound").forGetter(StaffType::noManaSound),
              CRYSTAL_BALL_CODEC.fieldOf("crystal_ball").forGetter(type -> Pair.of(type.crystalBallTheme(), type.crystalBallEntry())))
          .apply(instance, (strokeSound, failSound, noManaSound, crystalBall) -> new StaffType(strokeSound, failSound, noManaSound,
              crystalBall.getFirst(), crystalBall.getSecond())));

  public static final StreamCodec<RegistryFriendlyByteBuf, Holder<StaffType>> STREAM_CODEC =
      ByteBufCodecs.holderRegistry(ModRegistries.STAFF_TYPE_KEY);

  public static final class Builder {
    private @Nullable Identifier icon;
    private @Nullable Component title;
    private @Nullable Component description;
    private @Nullable Holder<SoundEvent> strokeSound;
    private @Nullable Holder<SoundEvent> failSound;
    private @Nullable Holder<SoundEvent> noManaSound;
    private int connectionColor = CrystalBallTheme.DEFAULT_CONNECTION_COLOR;
    private @Nullable Identifier crystalBallBackground;

    private Builder() {}

    public Builder icon(Identifier icon) {
      this.icon = icon;
      return this;
    }

    public Builder title(Component title) {
      this.title = title;
      return this;
    }

    public Builder description(Component description) {
      this.description = description;
      return this;
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
      return new StaffType(Optional.ofNullable(strokeSound), Optional.ofNullable(failSound), Optional.ofNullable(noManaSound),
          new CrystalBallTheme(connectionColor, Optional.ofNullable(crystalBallBackground)),
          new CrystalBallEntry(required(icon, "icon"), required(title, "title"), Optional.ofNullable(description)));
    }

    private static <T> T required(@Nullable T value, String name) {
      if (value == null) {
        throw new IllegalStateException("Missing required field '" + name + "'");
      }
      return value;
    }
  }
}
