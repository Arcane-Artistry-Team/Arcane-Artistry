package gragongit.arcaneartistry.common.spell;

import com.mojang.serialization.MapCodec;

public record SpellEffectType<T extends SpellEffect>(MapCodec<T> codec) {
}
