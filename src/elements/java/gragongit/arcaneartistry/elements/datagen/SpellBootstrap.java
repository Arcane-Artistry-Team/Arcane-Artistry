package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import gragongit.arcaneartistry.elements.common.spells.effects.FireballEffect;
import gragongit.arcaneartistry.elements.common.staffs.StaffTypes;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;

public final class SpellBootstrap {
  static void bootstrapSpells(BootstrapContext<Spell> context) {
    HolderGetter<StaffType> staffTypes = context.lookup(ModRegistries.STAFF_TYPE_KEY);
    registerSpell(context, "fireball_spell",
        new Spell(staffTypes, StaffTypes.FIRE_KEY, CastPattern.of("UD"), new FireballEffect(10), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST));
  }

  private static void registerSpell(BootstrapContext<Spell> context, String spellId, Spell spell) {
    context.register(ResourceKey.create(ModRegistries.SPELL_KEY, ArcaneArtistryElements.id(spellId)), spell);
  }
}
