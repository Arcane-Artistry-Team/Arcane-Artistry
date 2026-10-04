package gragongit.arcaneartistry.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gragongit.arcaneartistry.common.api.CastProgressEvents;
import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.crystalball.CrystalBallAttachments;
import gragongit.arcaneartistry.common.mana.ManaAttachments;
import gragongit.arcaneartistry.common.mana.ManaAttributes;
import gragongit.arcaneartistry.common.mana.ManaState;
import gragongit.arcaneartistry.common.network.ModNetworking;
import gragongit.arcaneartistry.common.registry.ModBlockEntities;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModItems;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.SpellHandler;
import gragongit.arcaneartistry.common.staff.StaffCastAttachments;
import gragongit.arcaneartistry.common.staff.StaffInteractionHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;

public class ArcaneArtistry implements ModInitializer {
  public static final String MOD_ID = "arcane-artistry";
  public static final Logger LOGGER = LoggerFactory.getLogger("Arcane Artistry");

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing Arcane Artistry");

    ArcaneArtistryConfig.load();
    ModRegistries.register();
    ModNetworking.register();
    ModDataComponents.register();
    ModBlocks.register();
    ModBlockEntities.register();
    ModItems.register();
    StaffCastAttachments.register();
    StaffInteractionHandler.register();
    CrystalBallAttachments.register();
    ManaAttributes.register();
    ManaAttachments.register();

    SpellHandler.init();

    ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(player -> ManaState.of(player).tick()));

    CastProgressEvents.STOP.register(this::testLog);
  }

  public static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }

  private void testLog(CastProgressContext castContext) {
    ArcaneArtistry.LOGGER.info(castContext.castPattern().toString());
  }
}
