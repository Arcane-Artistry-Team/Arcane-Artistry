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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;

public final class SpellBootstrap {
  static void bootstrapSpells(BootstrapContext<Spell> context) {
    HolderGetter<StaffType> staffTypes = context.lookup(ModRegistries.STAFF_TYPE_KEY);
    registerSpell(context, "fireball",
        Spell
            .builder()
            .staffType(staffTypes.getOrThrow(StaffTypes.FIRE_KEY))
            .pattern(CastPattern.of("UD"))
            .effect(new FireballEffect(3))
            .manaCost(30)
            .icon(Identifier.withDefaultNamespace("textures/item/fire_charge.png"))
            .title(Component.translatable("spell.arcane-artistry-elements.fireball"))
            .description(Component.translatable("spell.arcane-artistry-elements.fireball.description"))
            .castSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST));
    registerSpell(context, "waterbomb",
        Spell
            .builder()
            .staffType(staffTypes.getOrThrow(StaffTypes.WATER_KEY))
            .pattern(CastPattern.of("LR"))
            .effect(new FireballEffect(1))
            .manaCost(15)
            .icon(Identifier.withDefaultNamespace("textures/item/nether_star.png"))
            .title(Component.translatable("spell.arcane-artistry-elements.waterbomb"))
            .description(Component.translatable("spell.arcane-artistry-elements.waterbomb.description"))
            .castSound(SoundEvents.PLAYER_SPLASH));
  }

  private static void registerSpell(BootstrapContext<Spell> context, String spellId, Spell.Builder spell) {
    context.register(ResourceKey.create(ModRegistries.SPELL_KEY, ArcaneArtistryElements.id(spellId)), spell.build());
  }
}
