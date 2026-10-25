package gragongit.arcaneartistry.common;

import gragongit.arcaneartistry.common.worldgen.CrystalCavesRegion;
import terrablender.api.Regions;
import terrablender.api.TerraBlenderApi;

public class ArcaneArtistryTerraBlender implements TerraBlenderApi {
  /**
   * The weight of the crystal caves region relative to the vanilla region, which has a weight of 10.
   */
  private static final int CRYSTAL_CAVES_WEIGHT = 5;

  @Override
  public void onTerraBlenderInitialized() {
    Regions.register(new CrystalCavesRegion(ArcaneArtistry.id("crystal_caves"), CRYSTAL_CAVES_WEIGHT));
  }
}
