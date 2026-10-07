package gragongit.arcaneartistry.elements.common.staffs;

import gragongit.arcaneartistry.common.staff.StaffItems;
import net.minecraft.world.item.Items;

public final class Staffs {
  public static void init() {
    StaffItems.register(Items.BLAZE_ROD, StaffTypes.FIRE_KEY);
    StaffItems.register(Items.BREEZE_ROD, StaffTypes.WATER_KEY);
  }

  private Staffs() {}
}
