package gragongit.arcaneartistry.datagen.guidebook;

import java.util.Optional;
import java.util.function.Consumer;
import com.mojang.datafixers.util.Either;
import gragongit.arcaneartistry.common.guidebook.BookCategory;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import gragongit.arcaneartistry.common.guidebook.page.RecipePage;
import gragongit.arcaneartistry.common.guidebook.page.ShowcasePage;
import gragongit.arcaneartistry.common.guidebook.page.TextPage;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * Builds guide book content while generating translation keys, so the English text only has to be written once.
 *
 * <p>
 * Keys follow {@code book.<namespace>.category.<id>} and {@code book.<namespace>.entry.<id>.<part>}.
 */
public final class BookContext {
  public interface Output {
    default void category(ResourceKey<BookCategory> key, BookCategory category) {}

    default void entry(ResourceKey<BookEntry> key, BookEntry entry) {}

    default void translation(String key, String english) {}
  }

  private final String namespace;
  private final Output output;

  public BookContext(String namespace, Output output) {
    this.namespace = namespace;
    this.output = output;
  }

  public static ResourceKey<BookCategory> categoryKey(Identifier id) {
    return ResourceKey.create(ModRegistries.BOOK_CATEGORY_KEY, id);
  }

  public static ResourceKey<BookEntry> entryKey(Identifier id) {
    return ResourceKey.create(ModRegistries.BOOK_ENTRY_KEY, id);
  }

  public ResourceKey<BookCategory> category(String id, Consumer<CategoryBuilder> spec) {
    CategoryBuilder builder = new CategoryBuilder("book." + namespace + ".category." + id);
    spec.accept(builder);
    ResourceKey<BookCategory> key = categoryKey(Identifier.fromNamespaceAndPath(namespace, id));
    output.category(key, builder.category.build());
    return key;
  }

  public ResourceKey<BookEntry> entry(String id, Consumer<EntryBuilder> spec) {
    EntryBuilder builder = new EntryBuilder("book." + namespace + ".entry." + id);
    spec.accept(builder);
    ResourceKey<BookEntry> key = entryKey(Identifier.fromNamespaceAndPath(namespace, id));
    output.entry(key, builder.entry.build());
    return key;
  }

  private Component translatable(String key, String english) {
    output.translation(key, english);
    return Component.translatable(key);
  }

  public final class CategoryBuilder {
    private final String prefix;
    private final BookCategory.Builder category = BookCategory.builder();

    private CategoryBuilder(String prefix) {
      this.prefix = prefix;
    }

    public CategoryBuilder title(String english) {
      category.title(translatable(prefix, english));
      return this;
    }

    public CategoryBuilder icon(Item icon) {
      category.icon(icon);
      return this;
    }

    public CategoryBuilder sort(int sort) {
      category.sort(sort);
      return this;
    }

    public CategoryBuilder textureBackground(Identifier texture) {
      category.textureBackground(texture);
      return this;
    }

    public CategoryBuilder shaderBackground(Identifier shader) {
      category.shaderBackground(shader);
      return this;
    }

    public CategoryBuilder connectionColor(int color) {
      category.connectionColor(color);
      return this;
    }
  }

  public final class EntryBuilder {
    private final String prefix;
    private final BookEntry.Builder entry = BookEntry.builder();
    private int pageCount;

    private EntryBuilder(String prefix) {
      this.prefix = prefix;
    }

    public EntryBuilder category(ResourceKey<BookCategory> category) {
      entry.category(category);
      return this;
    }

    /** Position on the category map, in grid cells. */
    public EntryBuilder position(int x, int y) {
      entry.position(x, y);
      return this;
    }

    public EntryBuilder icon(Item icon) {
      entry.icon(icon);
      return this;
    }

    public EntryBuilder title(String english) {
      entry.title(translatable(prefix + ".title", english));
      return this;
    }

    public EntryBuilder description(String english) {
      entry.description(translatable(prefix + ".description", english));
      return this;
    }

    public EntryBuilder parent(ResourceKey<BookEntry> parent) {
      entry.parent(parent);
      return this;
    }

    public EntryBuilder advancement(Identifier advancement) {
      entry.advancement(advancement);
      return this;
    }

    public EntryBuilder text(String english) {
      String page = nextPage();
      entry.page(new TextPage(Optional.empty(), translatable(page + ".text", english)));
      return this;
    }

    public EntryBuilder text(String title, String english) {
      String page = nextPage();
      entry.page(new TextPage(Optional.of(translatable(page + ".title", title)), translatable(page + ".text", english)));
      return this;
    }

    public EntryBuilder recipe(Identifier recipe, String english) {
      String page = nextPage();
      entry
          .page(new RecipePage(ResourceKey.create(Registries.RECIPE, recipe), Optional.empty(),
              Optional.of(translatable(page + ".text", english))));
      return this;
    }

    public EntryBuilder showcase(Item item, String title, String english) {
      String page = nextPage();
      entry
          .page(new ShowcasePage(Either.left(BuiltInRegistries.ITEM.wrapAsHolder(item)), Optional.of(translatable(page + ".title", title)),
              Optional.of(translatable(page + ".text", english))));
      return this;
    }

    public EntryBuilder image(Identifier texture, int width, int height, String title, String english) {
      String page = nextPage();
      entry
          .page(new ShowcasePage(Either.right(new ShowcasePage.Image(texture, width, height)),
              Optional.of(translatable(page + ".title", title)), Optional.of(translatable(page + ".text", english))));
      return this;
    }

    private String nextPage() {
      return prefix + ".page" + pageCount++;
    }
  }
}
