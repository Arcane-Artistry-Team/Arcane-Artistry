package gragongit.arcaneartistry.datagen.guidebook;

import java.util.Comparator;
import java.util.List;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import gragongit.arcaneartistry.common.presentation.Presentation;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.staff.StaffItems;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec2;

public class StaffTypeEntryProvider extends EntryProvider {
  public static final String LOCKED = "guide_book.arcane-artistry.staff_type.locked";

  private static final Comparator<Holder.Reference<?>> BY_ID = Comparator.comparing(holder -> holder.key().identifier());

  protected final Holder.Reference<StaffType> staffType;

  public StaffTypeEntryProvider(CategoryProviderBase parent, Holder.Reference<StaffType> staffType) {
    super(parent);
    this.staffType = staffType;
  }

  public static List<Holder.Reference<StaffType>> staffTypes(HolderLookup.Provider registries, String namespace) {
    return registries
        .lookupOrThrow(ModRegistries.STAFF_TYPE_KEY)
        .listElements()
        .filter(staffType -> staffType.key().identifier().getNamespace().equals(namespace))
        .sorted(BY_ID)
        .toList();
  }

  protected List<Holder.Reference<Spell>> spells() {
    return registries()
        .lookupOrThrow(ModRegistries.SPELL_KEY)
        .listElements()
        .filter(spell -> spell.value().staffType().is(staffType.key()))
        .sorted(BY_ID)
        .toList();
  }

  @Override
  public BookEntryModel generate(Vec2 location) {
    context().entry(entryId());
    Presentation presentation = staffType.value().presentation();
    BookEntryModel model = BookEntryModel
        .create(modLoc(context().categoryId() + "/" + entryId()), translationKey(presentation.title(), context().entryName()))
        .withDescription(presentation.description().map(text -> translationKey(text, context().entryDescription())).orElse(""))
        .withIcon(entryIcon())
        .withLocation(location)
        .withEntryBackground(entryBackground());
    entry = additionalSetup(model);
    generatePages();
    parent.add(entry);
    return entry;
  }

  private String translationKey(Component text, String fallbackKey) {
    if (text.getContents() instanceof TranslatableContents translatable && translatable.getArgs().length == 0 && text.getSiblings().isEmpty()) {
      return translatable.getKey();
    }
    add(fallbackKey, text.getString());
    return fallbackKey;
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    List<Item> items = StaffItems.of(staffType.key());
    if (items.isEmpty()) {
      return entry;
    }
    Component tooltip = Component.translatable(LOCKED, ComponentUtils.formatList(items, item -> Component.translatable(item.getDescriptionId())));
    return entry.withCondition(condition().researchNodeUnlocked(GuideBookResearch.staffObtained(staffType.key())).withTooltip(tooltip));
  }

  @Override
  protected void generatePages() {
    StaffTypePageModel staffTypePage = StaffTypePageModel.create(staffType.key());
    StaffItems.of(staffType.key()).forEach(staffTypePage::withAssociatedItem);
    page(staffTypePage);
    spells().forEach(spell -> page(SpellPageModel.create(spell.key())));
  }

  @Override
  protected String entryName() {
    throw new UnsupportedOperationException("The name comes from the staff type");
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(staffType.value().presentation().icon());
  }

  @Override
  protected String entryId() {
    return staffType.key().identifier().getPath();
  }
}
