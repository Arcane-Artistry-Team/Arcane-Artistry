package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import gragongit.arcaneartistry.elements.common.FireballEffect;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public final class SpellBootstrap {
  static void bootstrapSpells(BootstrapContext<Spell> context) {
    registerSpell(context, "fireball_spell", new Spell(ArcaneArtistryElements.FIRE, CastPattern.of("UD"), new FireballEffect(10)));
  }

  private static void registerSpell(BootstrapContext<Spell> context, String spellId, Spell spell) {
    context.register(ResourceKey.create(ModRegistries.SPELL_KEY, ArcaneArtistryElements.id(spellId)), spell);
  }
}
