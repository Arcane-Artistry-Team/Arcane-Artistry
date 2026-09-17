package gragongit.arcaneartistry.common.registry;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.spell.SpellEffectType;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class ModRegistries {
  private ModRegistries() {}

  public static final ResourceKey<Registry<StaffType>> STAFF_TYPE_KEY = ResourceKey.createRegistryKey(ArcaneArtistry.id("staff_type"));

  public static final Registry<StaffType> STAFF_TYPES =
      FabricRegistryBuilder.create(STAFF_TYPE_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

  public static final ResourceKey<Registry<SpellEffectType<?>>> SPELL_EFFECT_TYPE_KEY =
      ResourceKey.createRegistryKey(ArcaneArtistry.id("spell_effect_type"));

  public static final Registry<SpellEffectType<?>> SPELL_EFFECT_TYPES =
      FabricRegistryBuilder.create(SPELL_EFFECT_TYPE_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

  public static final ResourceKey<Registry<Spell>> SPELL_KEY = ResourceKey.createRegistryKey(ArcaneArtistry.id("spell"));

  public static void register() {
    ArcaneArtistry.LOGGER.info("Registering registries");
    DynamicRegistries.registerSynced(SPELL_KEY, Spell.CODEC);
  }
}
