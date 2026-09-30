package gragongit.arcaneartistry.common.spell;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.crystalball.CrystalBallEntry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record Spell(Holder<StaffType> staffType, CastPattern pattern, SpellEffect effect, int manaCost, CrystalBallEntry crystalBallEntry,
    Optional<Holder<SoundEvent>> castSound) {

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private static final Codec<SpellEffect> EFFECT_CODEC = ModRegistries.SPELL_EFFECT_TYPES
      .byNameCodec()
      .dispatch("type", SpellEffect::type, type -> ((SpellEffectType<SpellEffect>) type).codec());

  public static final Codec<Spell> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(
              RegistryFileCodec
                  .create(ModRegistries.STAFF_TYPE_KEY, StaffType.CODEC, false)
                  .fieldOf("staff_type")
                  .forGetter(Spell::staffType),
              CastPattern.CODEC.fieldOf("pattern").forGetter(Spell::pattern), EFFECT_CODEC.fieldOf("effect").forGetter(Spell::effect),
              ExtraCodecs.NON_NEGATIVE_INT.fieldOf("mana_cost").forGetter(Spell::manaCost),
              CrystalBallEntry.CODEC.fieldOf("crystal_ball").forGetter(Spell::crystalBallEntry),
              SoundEvent.CODEC.optionalFieldOf("cast_sound").forGetter(Spell::castSound))
          .apply(instance, Spell::new));

  public static final class Builder {
    private @Nullable Holder<StaffType> staffType;
    private @Nullable CastPattern pattern;
    private @Nullable SpellEffect effect;
    private @Nullable Integer manaCost;
    private @Nullable Identifier icon;
    private @Nullable Component title;
    private @Nullable Component description;
    private @Nullable Holder<SoundEvent> castSound;

    private Builder() {}

    public Builder staffType(Holder<StaffType> staffType) {
      this.staffType = staffType;
      return this;
    }

    public Builder pattern(CastPattern pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder effect(SpellEffect effect) {
      this.effect = effect;
      return this;
    }

    public Builder manaCost(int manaCost) {
      this.manaCost = manaCost;
      return this;
    }

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

    public Builder castSound(SoundEvent castSound) {
      this.castSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(castSound);
      return this;
    }

    public Spell build() {
      return new Spell(required(staffType, "staffType"), required(pattern, "pattern"), required(effect, "effect"),
          required(manaCost, "manaCost"),
          new CrystalBallEntry(required(icon, "icon"), required(title, "title"), Optional.ofNullable(description)),
          Optional.ofNullable(castSound));
    }

    private static <T> T required(@Nullable T value, String name) {
      if (value == null) {
        throw new IllegalStateException("Missing required field '" + name + "'");
      }
      return value;
    }
  }
}
