package gragongit.arcaneartistry.client;

import gragongit.arcaneartistry.client.staff.StaffInteractionClientHandler;
import net.fabricmc.api.ClientModInitializer;

public class ArcaneArtistryClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    StaffInteractionClientHandler.register();
  }
}
