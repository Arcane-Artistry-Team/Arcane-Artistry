package gragongit.arcaneartistry.datagen.guidebook;

import com.klikli_dev.modonomicon.api.datagen.book.page.BookPageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import gragongit.arcaneartistry.common.guidebook.StaffTypePage;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

public class StaffTypePageModel extends BookPageModel<StaffTypePageModel> {
  private final ResourceKey<StaffType> staffType;

  protected StaffTypePageModel(ResourceKey<StaffType> staffType) {
    super(StaffTypePage.ID);
    this.staffType = staffType;
  }

  public static StaffTypePageModel create(ResourceKey<StaffType> staffType) {
    return new StaffTypePageModel(staffType).withId(staffType.identifier().getPath());
  }

  @Override
  public BookPage toBookPage(HolderLookup.Provider provider) {
    return new StaffTypePage(staffType, id, condition(provider), associatedItems);
  }
}
