package gragongit.arcaneartistry.datagen.guidebook.basics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class BasicsCategory extends CategoryProvider {
  public static final String ID = "basics";

  public BasicsCategory(ModonomiconProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generateEntries() {
    BookEntryModel guideBook = new GuideBookEntry(this).generate();
    layout().entry(guideBook).at(0, 0);

    BookEntryModel orbRing = child(new OrbRingEntry(this).generate(), guideBook);
    layout().entry(orbRing).at(3, -1);

    BookEntryModel lapisCrystal = child(new LapisCrystalEntry(this).generate(), guideBook);
    layout().entry(lapisCrystal).at(3, 1);

    BookEntryModel crystalBall = child(new CrystalBallEntry(this).generate(), orbRing, lapisCrystal);
    layout().entry(crystalBall).at(6, 0);

    BookEntryModel staffs = child(new StaffsEntry(this).generate(), crystalBall);
    layout().entry(staffs).at(9, 0);

    BookEntryModel mana = child(new ManaEntry(this).generate(), staffs);
    layout().entry(mana).at(12, 0);
  }

  private BookEntryModel child(BookEntryModel entry, BookEntryModel... parents) {
    for (BookEntryModel parent : parents) {
      entry.withParent(parent(parent));
    }
    return entry.hideWhileLocked(true);
  }

  @Override
  protected String categoryName() {
    return "Arcane Basics";
  }

  @Override
  protected BookIconModel categoryIcon() {
    return BookIconModel.create(Items.ENCHANTED_BOOK);
  }

  @Override
  public String categoryId() {
    return ID;
  }
}
