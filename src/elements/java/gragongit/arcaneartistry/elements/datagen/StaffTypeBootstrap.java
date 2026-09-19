package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.staffs.StaffTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;

public final class StaffTypeBootstrap {
  static void bootstrapStaffTypes(BootstrapContext<StaffType> context) {
    context.register(StaffTypes.FIRE_KEY, new StaffType(SoundEvents.FIRECHARGE_USE, SoundEvents.FIRE_EXTINGUISH));
  }
}
