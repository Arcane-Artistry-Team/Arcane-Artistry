package gragongit.arcaneartistry.common.guidebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.guidebook.page.BookPage;
import gragongit.arcaneartistry.common.guidebook.page.BookPageType;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public record BookEntry(ResourceKey<BookCategory> category, int x, int y, Holder<Item> icon, Component title,
    Optional<Component> description, List<ResourceKey<BookEntry>> parents, Optional<Identifier> advancement, List<BookPage> pages) {

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private static final Codec<BookPage> PAGE_CODEC = ModRegistries.BOOK_PAGE_TYPES
      .byNameCodec()
      .dispatch("type", BookPage::type, type -> ((BookPageType<BookPage>) type).codec());

  public static final Codec<BookEntry> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(ResourceKey.codec(ModRegistries.BOOK_CATEGORY_KEY).fieldOf("category").forGetter(BookEntry::category),
              Codec.INT.fieldOf("x").forGetter(BookEntry::x), Codec.INT.fieldOf("y").forGetter(BookEntry::y),
              Item.CODEC.fieldOf("icon").forGetter(BookEntry::icon),
              ComponentSerialization.CODEC.fieldOf("title").forGetter(BookEntry::title),
              ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(BookEntry::description),
              ResourceKey.codec(ModRegistries.BOOK_ENTRY_KEY).listOf().optionalFieldOf("parents", List.of()).forGetter(BookEntry::parents),
              Identifier.CODEC.optionalFieldOf("advancement").forGetter(BookEntry::advancement),
              PAGE_CODEC.listOf().fieldOf("pages").forGetter(BookEntry::pages))
          .apply(instance, BookEntry::new));

  public static final class Builder {
    private @Nullable ResourceKey<BookCategory> category;
    private int x;
    private int y;
    private @Nullable Holder<Item> icon;
    private @Nullable Component title;
    private @Nullable Component description;
    private final List<ResourceKey<BookEntry>> parents = new ArrayList<>();
    private @Nullable Identifier advancement;
    private final List<BookPage> pages = new ArrayList<>();

    private Builder() {}

    public Builder category(ResourceKey<BookCategory> category) {
      this.category = category;
      return this;
    }

    public Builder position(int x, int y) {
      this.x = x;
      this.y = y;
      return this;
    }

    public Builder icon(Item icon) {
      this.icon = BuiltInRegistries.ITEM.wrapAsHolder(icon);
      return this;
    }

    public Builder title(Component title) {
      this.title = title;
      return this;
    }

    public Builder description(Component description) {
      this.description = description;
      return this;
    }

    public Builder parent(ResourceKey<BookEntry> parent) {
      this.parents.add(parent);
      return this;
    }

    public Builder advancement(Identifier advancement) {
      this.advancement = advancement;
      return this;
    }

    public Builder page(BookPage page) {
      this.pages.add(page);
      return this;
    }

    public BookEntry build() {
      if (pages.isEmpty()) {
        throw new IllegalStateException("A book entry needs at least one page");
      }
      return new BookEntry(required(category, "category"), x, y, required(icon, "icon"), required(title, "title"),
          Optional.ofNullable(description), List.copyOf(parents), Optional.ofNullable(advancement), List.copyOf(pages));
    }

    private static <T> T required(@Nullable T value, String name) {
      if (value == null) {
        throw new IllegalStateException("Missing required field '" + name + "'");
      }
      return value;
    }
  }
}
