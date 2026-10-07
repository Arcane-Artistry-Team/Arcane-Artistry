package gragongit.arcaneartistry.datagen.guidebook;

import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.guidebook.GuideBook;
import gragongit.arcaneartistry.datagen.guidebook.basics.BasicsCategory;
import net.minecraft.resources.Identifier;

public class GuideBookProvider extends SingleBookSubProvider {

  public GuideBookProvider() {
    super(GuideBook.ID.getPath(), ArcaneArtistry.MOD_ID);
  }

  public static Identifier shaderBackground(Identifier shader) {
    return shader.withPath(path -> "shaders/" + path + ".fsh");
  }

  @Override
  protected BookModel additionalSetup(BookModel book) {
    return book.withModel(Identifier.withDefaultNamespace("enchanted_book")).withGenerateEntryHierarchyResearch(true);
  }

  @Override
  protected void registerDefaultMacros() {}

  @Override
  protected void generateCategories() {
    add(new BasicsCategory(this).generate());
  }

  @Override
  protected String bookName() {
    return "Arcane Compendium";
  }

  @Override
  protected String bookTooltip() {
    return "A record of everything you have learned about the arcane.";
  }
}
