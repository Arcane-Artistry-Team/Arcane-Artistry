package gragongit.arcaneartistry.elements.datagen.guidebook;

import java.util.List;
import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookCategoryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import gragongit.arcaneartistry.common.staff.StaffType;
import gragongit.arcaneartistry.datagen.guidebook.GuideBookProvider;
import gragongit.arcaneartistry.datagen.guidebook.StaffTypeEntryProvider;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Items;

public class ElementsCategory extends CategoryProvider {
  public static final String ID = "elements";

  public ElementsCategory(ModonomiconProviderBase parent) {
    super(parent);
  }

  @Override
  protected BookCategoryModel additionalSetup(BookCategoryModel category) {
    return category
        .withCondition(ElementsResearch.STAFFS_READ)
        .withBackground(GuideBookProvider.shaderBackground(ArcaneArtistryElements.id("crystal_ball/tidal_galaxy")));
  }

  @Override
  protected void generateEntries() {
    List<Holder.Reference<StaffType>> staffTypes = StaffTypeEntryProvider.staffTypes(registries(), ArcaneArtistryElements.MOD_ID);
    for (int i = 0; i < staffTypes.size(); i++) {
      BookEntryModel entry = new StaffTypeEntryProvider(this, staffTypes.get(i)).generate();
      layout().entry(entry).at(0, 2 * i - (staffTypes.size() - 1));
    }
  }

  @Override
  protected String categoryName() {
    return "Elements";
  }

  @Override
  protected BookIconModel categoryIcon() {
    return BookIconModel.create(Items.BLAZE_ROD);
  }

  @Override
  public String categoryId() {
    return ID;
  }
}
