package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.datagen.SpellProvider;
import gragongit.arcaneartistry.datagen.StaffTypeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public class ArcaneArtistryElementsDataGenerator implements DataGeneratorEntrypoint {

  @Override
  public void onInitializeDataGenerator(FabricDataGenerator generator) {
    FabricDataGenerator.Pack pack = generator.createPack();
    pack.addProvider(StaffTypeProvider::new);
    pack.addProvider(SpellProvider::new);
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    registryBuilder.add(ModRegistries.STAFF_TYPE_KEY, StaffTypeBootstrap::bootstrapStaffTypes);
    registryBuilder.add(ModRegistries.SPELL_KEY, SpellBootstrap::bootstrapSpells);
  }
}
