package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import gragongit.arcaneartistry.elements.common.spells.effects.FireballEffect;
import gragongit.arcaneartistry.elements.common.staffs.StaffTypes;
import net.minecraft.core.Holder.Reference;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public final class SpellBootstrap {
  static void bootstrapSpells(BootstrapContext<Spell> context) {
    registerSpell(context, "fireball_spell",
        new Spell(staffType(context, StaffTypes.FIRE_KEY), CastPattern.of("UD"), new FireballEffect(10)));
  }

  private static void registerSpell(BootstrapContext<Spell> context, String spellId, Spell spell) {
    context.register(ResourceKey.create(ModRegistries.SPELL_KEY, ArcaneArtistryElements.id(spellId)), spell);
  }

  private static Reference<StaffType> staffType(BootstrapContext<Spell> context, ResourceKey<StaffType> staffType) {
    return context.lookup(ModRegistries.STAFF_TYPE_KEY).getOrThrow(staffType);
  }
}
