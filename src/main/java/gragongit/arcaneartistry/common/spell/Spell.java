package gragongit.arcaneartistry.common.spell;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

public record Spell(Holder<StaffType> staffType, CastPattern pattern, SpellEffect effect, Optional<Holder<SoundEvent>> castSound) {

  public Spell(HolderGetter<StaffType> staffTypes, ResourceKey<StaffType> staffType, CastPattern pattern, SpellEffect effect,
      SoundEvent castSound) {
    this(staffTypes.getOrThrow(staffType), pattern, effect, castSound);
  }

  public Spell(Holder<StaffType> staffType, CastPattern pattern, SpellEffect effect, SoundEvent castSound) {
    this(staffType, pattern, effect, Optional.ofNullable(castSound).map(BuiltInRegistries.SOUND_EVENT::wrapAsHolder));
  }

  @SuppressWarnings("unchecked")
  private static final Codec<SpellEffect> EFFECT_CODEC = ModRegistries.SPELL_EFFECT_TYPES
      .byNameCodec()
      .dispatch("type", SpellEffect::type, type -> ((SpellEffectType<SpellEffect>) type).codec());

  public static final Codec<Spell> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(RegistryFileCodec.create(ModRegistries.STAFF_TYPE_KEY, StaffType.CODEC).fieldOf("staff_type").forGetter(Spell::staffType),
              CastPattern.CODEC.fieldOf("pattern").forGetter(Spell::pattern), EFFECT_CODEC.fieldOf("effect").forGetter(Spell::effect),
              SoundEvent.CODEC.optionalFieldOf("cast_sound").forGetter(Spell::castSound))
          .apply(instance, Spell::new));
}
