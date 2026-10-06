package gragongit.arcaneartistry.common.guidebook.page;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.core.Registry;

public final class BookPageTypes {
  public static final BookPageType<TextPage> TEXT = register("text", new BookPageType<>(TextPage.CODEC));
  public static final BookPageType<RecipePage> RECIPE = register("recipe", new BookPageType<>(RecipePage.CODEC));
  public static final BookPageType<ShowcasePage> SHOWCASE = register("showcase", new BookPageType<>(ShowcasePage.CODEC));

  private BookPageTypes() {}

  private static <P extends BookPage> BookPageType<P> register(String path, BookPageType<P> type) {
    return Registry.register(ModRegistries.BOOK_PAGE_TYPES, ArcaneArtistry.id(path), type);
  }

  public static void register() {}
}
