package gragongit.arcaneartistry.elements.common.staffs;

import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.Staff;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Holder.Reference;
import net.minecraft.world.item.Items;

public final class Staffs {
  public static void init() {
    DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.BLAZE_ROD, (builder, lookupProvider, item) -> {
      Reference<StaffType> fire = lookupProvider.lookupOrThrow(ModRegistries.STAFF_TYPE_KEY).getOrThrow(StaffTypes.FIRE_KEY);
      builder.set(ModDataComponents.STAFF, new Staff(fire));
    }));
    DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.BREEZE_ROD, (builder, lookupProvider, item) -> {
      Reference<StaffType> water = lookupProvider.lookupOrThrow(ModRegistries.STAFF_TYPE_KEY).getOrThrow(StaffTypes.WATER_KEY);
      builder.set(ModDataComponents.STAFF, new Staff(water));
    }));
  }

  private Staffs() {}
}
