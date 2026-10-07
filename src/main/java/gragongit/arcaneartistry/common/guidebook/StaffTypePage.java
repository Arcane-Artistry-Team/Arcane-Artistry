package gragongit.arcaneartistry.common.guidebook;

import java.util.List;
import org.jspecify.annotations.Nullable;
import com.klikli_dev.modonomicon.book.BookTextHolder;
import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.entries.BookContentEntry;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookSpotlightPage;
import com.klikli_dev.modonomicon.client.gui.book.markdown.BookTextRenderer;
import com.klikli_dev.modonomicon.data.BookPageType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffItems;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class StaffTypePage extends BookSpotlightPage {
  public static final Identifier ID = ArcaneArtistry.id("staff_type");
  public static final String ITEMS = "guide_book.arcane-artistry.staff_type.items";
  public static final String UNKNOWN = "guide_book.arcane-artistry.staff_type.unknown";

  public static final MapCodec<StaffTypePage> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(ResourceKey.codec(ModRegistries.STAFF_TYPE_KEY).fieldOf("staff_type").forGetter(StaffTypePage::staffType),
              Codec.STRING.fieldOf("id").forGetter(BookPage::getId),
              BookCondition.CODEC.optionalFieldOf("condition", new BookNoneCondition()).forGetter(BookPage::getCondition),
              BookPage.ASSOCIATED_ITEMS_CODEC.optionalFieldOf("associated_items", List.of()).forGetter(BookPage::getAssociatedItems))
          .apply(instance, StaffTypePage::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, StaffTypePage> STREAM_CODEC = StreamCodec
      .composite(ResourceKey.streamCodec(ModRegistries.STAFF_TYPE_KEY), StaffTypePage::staffType, ByteBufCodecs.STRING_UTF8,
          BookPage::getId, BookCondition.STREAM_CODEC, BookPage::getCondition, BookPage.ASSOCIATED_ITEMS_STREAM_CODEC,
          BookPage::getAssociatedItems, StaffTypePage::new);

  private final ResourceKey<StaffType> staffType;
  private @Nullable StaffType value;

  public StaffTypePage(ResourceKey<StaffType> staffType, String id, BookCondition condition,
      List<Either<ItemStackTemplate, Ingredient>> associatedItems) {
    super(BookTextHolder.EMPTY, BookTextHolder.EMPTY, Either.left(new ItemStackTemplate(Items.BARRIER)), id, condition);
    setAssociatedItems(associatedItems);
    this.staffType = staffType;
  }

  public ResourceKey<StaffType> staffType() {
    return staffType;
  }

  @Override
  public BookPageType<?> type() {
    return GuideBookPages.STAFF_TYPE;
  }

  @Override
  public void build(Level level, BookContentEntry parentEntry, int pageNum) {
    List<Item> items = StaffItems.of(staffType);
    if (!items.isEmpty()) {
      item = items.size() == 1 ? Either.left(new ItemStackTemplate(items.getFirst())) : Either.right(Ingredient.of(items.stream()));
    }
    super.build(level, parentEntry, pageNum);
    value = GuideBookPages.lookup(level, ModRegistries.STAFF_TYPE_KEY, staffType).orElse(null);
    Component name = value != null ? value.presentation().title() : Component.literal(staffType.identifier().toString());
    title = GuideBookPages.title(book, name);
  }

  @Override
  public void prerenderMarkdown(BookTextRenderer textRenderer) {
    text = GuideBookPages.markdown(textRenderer, value != null ? markdown(value) : Component.translatable(UNKNOWN).getString());
    super.prerenderMarkdown(textRenderer);
  }

  private String markdown(StaffType type) {
    List<Item> items = StaffItems.of(staffType);
    String staffs = items.isEmpty() ? ""
        : Component.translatable(ITEMS, ComponentUtils.formatList(items, item -> new ItemStack(item).getHoverName())).getString();
    return GuideBookPages.paragraphs(type.presentation().description().map(Component::getString).orElse(""), staffs);
  }
}
