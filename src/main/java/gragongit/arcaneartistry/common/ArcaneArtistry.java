package gragongit.arcaneartistry.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gragongit.arcaneartistry.common.api.CastProgressEvents;
import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.network.ModNetworking;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.SpellHandler;
import gragongit.arcaneartistry.common.staff.StaffCastAttachments;
import gragongit.arcaneartistry.common.staff.StaffInteractionHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class ArcaneArtistry implements ModInitializer {
  public static final String MOD_ID = "arcane-artistry";
  public static final Logger LOGGER = LoggerFactory.getLogger("Arcane Artistry");

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing Arcane Artistry");

    ModRegistries.register();
    ModNetworking.register();
    ModDataComponents.register();
    StaffCastAttachments.register();
    StaffInteractionHandler.register();

    SpellHandler.init();

    CastProgressEvents.STOP.register(this::testLog);
  }

  public static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }

  private void testLog(CastProgressContext castContext) {
    ArcaneArtistry.LOGGER.info(castContext.castPattern().toString());
  }
}
