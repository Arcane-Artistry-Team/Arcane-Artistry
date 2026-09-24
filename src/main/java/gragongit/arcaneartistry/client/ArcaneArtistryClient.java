package gragongit.arcaneartistry.client;

import gragongit.arcaneartistry.client.crystalball.CrystalBallNodeState;
import gragongit.arcaneartistry.client.crystalball.CrystalBallScreen;
import gragongit.arcaneartistry.client.staff.StaffInteractionClientHandler;
import gragongit.arcaneartistry.common.crystalball.CrystalBallPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class ArcaneArtistryClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    StaffInteractionClientHandler.register();

    ClientPlayNetworking.registerGlobalReceiver(CrystalBallPayload.TYPE, (payload, context) -> {
      Minecraft.getInstance().gui.setScreen(new CrystalBallScreen(node -> CrystalBallNodeState.UNKNOWN));
    });
  }
}
