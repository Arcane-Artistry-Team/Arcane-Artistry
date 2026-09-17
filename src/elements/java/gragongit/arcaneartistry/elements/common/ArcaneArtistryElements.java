package gragongit.arcaneartistry.elements.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.SpellEffectType;
import gragongit.arcaneartistry.common.staff.Staff;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class ArcaneArtistryElements implements ModInitializer {
  public static final String MOD_ID = ArcaneArtistry.MOD_ID + "-elements";

  public static final Logger LOGGER = LoggerFactory.getLogger("Arcane Artistry Elements");

  public static final StaffType FIRE = Registry.register(ModRegistries.STAFF_TYPES, ArcaneArtistryElements.id("fire"), new StaffType());

  public static final SpellEffectType<FireballEffect> FIREBALL =
      Registry.register(ModRegistries.SPELL_EFFECT_TYPES, id("fireball"), new SpellEffectType<>(FireballEffect.CODEC));

  @Override
  public void onInitialize() {
    LOGGER.info("Arcane Artistry Elements!");

    DefaultItemComponentEvents.MODIFY
        .register(context -> context
            .modify(Items.STICK, builder -> builder.set(ModDataComponents.STAFF, new Staff(ArcaneArtistryElements.FIRE))));
  }

  public static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }
}
