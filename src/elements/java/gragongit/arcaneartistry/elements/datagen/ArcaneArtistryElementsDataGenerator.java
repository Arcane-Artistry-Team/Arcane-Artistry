package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.datagen.ArcaneArtistryDatagenModule;
import net.minecraft.core.RegistrySetBuilder;

public class ArcaneArtistryElementsDataGenerator implements ArcaneArtistryDatagenModule {

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    registryBuilder.add(ModRegistries.STAFF_TYPE_KEY, StaffTypeBootstrap::bootstrapStaffTypes);
    registryBuilder.add(ModRegistries.SPELL_KEY, SpellBootstrap::bootstrapSpells);
  }
}
