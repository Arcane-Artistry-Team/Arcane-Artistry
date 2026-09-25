package gragongit.arcaneartistry.elements.common.staffs;

import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import net.minecraft.resources.ResourceKey;

public final class StaffTypes {
  public static final ResourceKey<StaffType> FIRE_KEY = ResourceKey.create(ModRegistries.STAFF_TYPE_KEY, ArcaneArtistryElements.id("fire"));
  public static final ResourceKey<StaffType> WATER_KEY =
      ResourceKey.create(ModRegistries.STAFF_TYPE_KEY, ArcaneArtistryElements.id("water"));

  private StaffTypes() {}

  public static void init() {}
}
