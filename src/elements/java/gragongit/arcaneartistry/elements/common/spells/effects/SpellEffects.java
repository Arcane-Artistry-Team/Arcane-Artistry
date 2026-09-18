package gragongit.arcaneartistry.elements.common.spells.effects;

import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.SpellEffect;
import gragongit.arcaneartistry.common.spell.SpellEffectType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import net.minecraft.core.Registry;

public final class SpellEffects {
  public static final SpellEffectType<FireballEffect> FIREBALL = register("fireball", new SpellEffectType<>(FireballEffect.CODEC));

  private static <E extends SpellEffect> SpellEffectType<E> register(String path, SpellEffectType<E> type) {
    return Registry.register(ModRegistries.SPELL_EFFECT_TYPES, ArcaneArtistryElements.id(path), type);
  }

  private SpellEffects() {}

  public static void init() {}
}

