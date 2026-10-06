package gragongit.arcaneartistry.client.guidebook.page;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import gragongit.arcaneartistry.client.guidebook.GuideBookView;
import gragongit.arcaneartistry.common.guidebook.page.BookPage;
import gragongit.arcaneartistry.common.guidebook.page.BookPageType;
import gragongit.arcaneartistry.common.guidebook.page.BookPageTypes;
import net.minecraft.network.chat.Component;

/** Client side renderers per page type. Addons register their own page types here. */
public final class BookPageRenderers {
  private static final Map<BookPageType<?>, BookPageRenderer<?>> RENDERERS = new HashMap<>();

  private BookPageRenderers() {}

  public static void register() {
    register(BookPageTypes.TEXT, (page, view, width) -> new PageContent(page.title(), Optional.empty(), Optional.of(page.text())));
    register(BookPageTypes.SHOWCASE,
        (page, view, width) -> new PageContent(page.title(),
            Optional.of(page.display().map(ItemVisual::new, image -> new ImageVisual(image, width))), page.text()));
    register(BookPageTypes.RECIPE,
        (page, view, width) -> new PageContent(page.title(), Optional.of(RecipeVisual.of(page.recipe(), view.recipe(page.recipe()))),
            page.text()));
  }

  public static <P extends BookPage> void register(BookPageType<P> type, BookPageRenderer<P> renderer) {
    RENDERERS.put(type, renderer);
  }

  @SuppressWarnings("unchecked")
  public static PageContent content(BookPage page, GuideBookView view, int width) {
    BookPageRenderer<BookPage> renderer = (BookPageRenderer<BookPage>) RENDERERS.get(page.type());
    if (renderer == null) {
      return new PageContent(Optional.empty(), Optional.empty(), Optional.of(Component.literal("Unsupported page type")));
    }
    return renderer.content(page, view, width);
  }
}
