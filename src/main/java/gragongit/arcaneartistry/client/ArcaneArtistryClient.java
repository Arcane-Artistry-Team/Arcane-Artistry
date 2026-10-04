package gragongit.arcaneartistry.client;

import gragongit.arcaneartistry.client.crystalball.CrystalBallBackground;
import gragongit.arcaneartistry.client.mana.ManaHud;
import gragongit.arcaneartistry.client.network.ModClientNetworking;
import gragongit.arcaneartistry.client.renderer.OrbRingRenderer;
import gragongit.arcaneartistry.client.staff.StaffInteractionClientHandler;
import net.fabricmc.api.ClientModInitializer;

public class ArcaneArtistryClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    StaffInteractionClientHandler.register();
    CrystalBallBackground.register();
    ArcaneArtistryClientConfig.load();
    ManaHud.register();
    OrbRingRenderer.register();
    ModClientNetworking.register();
  }
}
