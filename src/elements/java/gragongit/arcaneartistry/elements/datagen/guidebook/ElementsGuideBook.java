package gragongit.arcaneartistry.elements.datagen.guidebook;

import com.klikli_dev.modonomicon.api.datagen.AddToBookSubProvider;
import gragongit.arcaneartistry.common.guidebook.GuideBook;

public class ElementsGuideBook extends AddToBookSubProvider {

  public ElementsGuideBook() {
    super(GuideBook.ID);
  }

  @Override
  protected void registerDefaultMacros() {}

  @Override
  protected void generateCategories() {
    add(new ElementsCategory(this).generate().withSortNumber(10));
  }
}
