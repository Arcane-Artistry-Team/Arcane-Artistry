package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import gragongit.arcaneartistry.elements.common.staffs.StaffTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public final class StaffTypeBootstrap {
  static void bootstrapStaffTypes(BootstrapContext<StaffType> context) {
    context
        .register(StaffTypes.FIRE_KEY,
            StaffType
                .builder(Identifier.withDefaultNamespace("textures/item/blaze_powder.png"))
                .strokeSound(SoundEvents.FIRECHARGE_USE)
                .failSound(SoundEvents.FIRE_EXTINGUISH)
                .noManaSound(SoundEvents.FURNACE_FIRE_CRACKLE)
                .connectionColor(0xB07D16)
                .crystalBallBackground(ArcaneArtistryElements.id("crystal_ball/inferno"))
                .build());
    context
        .register(StaffTypes.WATER_KEY,
            StaffType
                .builder(Identifier.withDefaultNamespace("textures/item/heart_of_the_sea.png"))
                .strokeSound(SoundEvents.POINTED_DRIPSTONE_DRIP_WATER)
                .failSound(SoundEvents.FIRE_EXTINGUISH)
                .noManaSound(SoundEvents.PLAYER_SWIM)
                .connectionColor(0x2A78C8)
                .crystalBallBackground(ArcaneArtistryElements.id("crystal_ball/tidal_galaxy"))
                .build());
  }
}
