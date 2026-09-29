package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import gragongit.arcaneartistry.elements.common.staffs.StaffTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public final class StaffTypeBootstrap {
  private static final int FIRE_CONNECTION_COLOR = 0xB07D16;
  private static final int WATER_CONNECTION_COLOR = 0x2A78C8;

  static void bootstrapStaffTypes(BootstrapContext<StaffType> context) {
    context
        .register(StaffTypes.FIRE_KEY,
            new StaffType(Identifier.withDefaultNamespace("textures/item/blaze_powder.png"), SoundEvents.FIRECHARGE_USE,
                SoundEvents.FIRE_EXTINGUISH, SoundEvents.FURNACE_FIRE_CRACKLE, FIRE_CONNECTION_COLOR,
                ArcaneArtistryElements.id("crystal_ball/inferno")));
    context
        .register(StaffTypes.WATER_KEY,
            new StaffType(Identifier.withDefaultNamespace("textures/item/heart_of_the_sea.png"), SoundEvents.POINTED_DRIPSTONE_DRIP_WATER,
                SoundEvents.FIRE_EXTINGUISH, SoundEvents.PLAYER_SWIM, WATER_CONNECTION_COLOR,
                ArcaneArtistryElements.id("crystal_ball/tidal_galaxy")));
  }
}
