package gragongit.arcaneartistry.elements.datagen;

import java.util.List;
import gragongit.arcaneartistry.common.staff.MeteorColors;
import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import gragongit.arcaneartistry.elements.common.staffs.StaffTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public final class StaffTypeBootstrap {
  private static final MeteorColors FIRE_METEOR = new MeteorColors(List.of(0xFFFFFF, 0xFFF3C4, 0xFFD27A),
      List.of(0xFFF8E0, 0xFFE066, 0xFFA53A, 0xE8552E, 0x9E2A3A));
  private static final MeteorColors WATER_METEOR = new MeteorColors(List.of(0xFFFFFF, 0xD6F4FF, 0x7FD3F2),
      List.of(0xEAFBFF, 0x9BE3F7, 0x4FB6E8, 0x2A78C8, 0x1B3F8A));

  static void bootstrapStaffTypes(BootstrapContext<StaffType> context) {
    context
        .register(StaffTypes.FIRE_KEY, new StaffType(Identifier.withDefaultNamespace("textures/item/blaze_powder.png"),
            SoundEvents.FIRECHARGE_USE, SoundEvents.FIRE_EXTINGUISH, SoundEvents.FURNACE_FIRE_CRACKLE, FIRE_METEOR,
            ArcaneArtistryElements.id("crystal_ball/inferno")));
    context
        .register(StaffTypes.WATER_KEY, new StaffType(Identifier.withDefaultNamespace("textures/item/heart_of_the_sea.png"),
            SoundEvents.POINTED_DRIPSTONE_DRIP_WATER, SoundEvents.FIRE_EXTINGUISH, SoundEvents.PLAYER_SWIM, WATER_METEOR,
            ArcaneArtistryElements.id("crystal_ball/tidal_galaxy")));
  }
}
