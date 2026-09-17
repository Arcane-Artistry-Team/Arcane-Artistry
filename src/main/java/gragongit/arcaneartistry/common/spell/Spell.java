package gragongit.arcaneartistry.common.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffType;

public record Spell(StaffType staffType, CastPattern pattern, SpellEffect effect) {

  @SuppressWarnings("unchecked")
  private static final Codec<SpellEffect> EFFECT_CODEC = ModRegistries.SPELL_EFFECT_TYPES
      .byNameCodec()
      .dispatch("type", SpellEffect::type, type -> ((SpellEffectType<SpellEffect>) type).codec());

  public static final Codec<Spell> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(ModRegistries.STAFF_TYPES.byNameCodec().fieldOf("staff_type").forGetter(Spell::staffType),
              CastPattern.CODEC.fieldOf("pattern").forGetter(Spell::pattern), EFFECT_CODEC.fieldOf("effect").forGetter(Spell::effect))
          .apply(instance, Spell::new));
}
