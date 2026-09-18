package gragongit.arcaneartistry.common.registry;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.staff.Staff;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModDataComponents {
  public static final DataComponentType<Staff> STAFF = Registry
      .register(BuiltInRegistries.DATA_COMPONENT_TYPE, ArcaneArtistry.id("staff"),
          DataComponentType.<Staff>builder().persistent(Staff.CODEC).build());

  public static void register() {}
}
