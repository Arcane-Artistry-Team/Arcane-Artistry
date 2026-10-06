package gragongit.arcaneartistry.elements.datagen;

import java.util.List;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.datagen.ArcaneArtistryDatagenModule;
import gragongit.arcaneartistry.datagen.guidebook.BookContent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public class ArcaneArtistryElementsDataGenerator implements ArcaneArtistryDatagenModule {

  @Override
  public void addProviders(FabricDataGenerator.Pack pack) {
    pack.addProvider(ElementsAdvancementProvider::new);
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    registryBuilder.add(ModRegistries.STAFF_TYPE_KEY, StaffTypeBootstrap::bootstrapStaffTypes);
    registryBuilder.add(ModRegistries.SPELL_KEY, SpellBootstrap::bootstrapSpells);
  }

  @Override
  public List<BookContent> bookContent() {
    return List.of(new ElementsBookContent());
  }
}
