package gragongit.arcaneartistry.elements.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.elements.common.spells.effects.SpellEffects;
import gragongit.arcaneartistry.elements.common.staffs.CastingEffects;
import gragongit.arcaneartistry.elements.common.staffs.StaffTypes;
import gragongit.arcaneartistry.elements.common.staffs.Staffs;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class ArcaneArtistryElements implements ModInitializer {
  public static final String MOD_ID = ArcaneArtistry.MOD_ID + "-elements";
  public static final Logger LOGGER = LoggerFactory.getLogger("Arcane Artistry Elements");

  @Override
  public void onInitialize() {
    LOGGER.info("Arcane Artistry Elements");

    StaffTypes.init();
    Staffs.init();
    SpellEffects.init();
    CastingEffects.init();
  }

  public static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }
}
