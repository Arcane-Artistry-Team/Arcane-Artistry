package gragongit.arcaneartistry.elements.common.staffs;

import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import net.minecraft.core.Registry;

public final class StaffTypes {
  public static final StaffType FIRE = register("fire", new StaffType());

  private static StaffType register(String path, StaffType type) {
    return Registry.register(ModRegistries.STAFF_TYPES, ArcaneArtistryElements.id(path), type);
  }

  private StaffTypes() {}

  public static void init() {}
}
