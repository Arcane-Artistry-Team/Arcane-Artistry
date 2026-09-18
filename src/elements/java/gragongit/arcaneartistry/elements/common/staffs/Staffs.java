package gragongit.arcaneartistry.elements.common.staffs;

import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.staff.Staff;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.world.item.Items;

public final class Staffs {
  public static void init() {
    DefaultItemComponentEvents.MODIFY
        .register(context -> context.modify(Items.STICK, builder -> builder.set(ModDataComponents.STAFF, new Staff(StaffTypes.FIRE))));
  }

  private Staffs() {}
}
