package gragongit.arcaneartistry.common.guidebook;

import java.util.Optional;
import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.book.BookTextHolder;
import com.klikli_dev.modonomicon.book.RenderedBookTextHolder;
import com.klikli_dev.modonomicon.client.gui.book.markdown.BookTextRenderer;
import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.modonomicon.registry.BookPageTypeRegistry;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class GuideBookPages {
  public static final BookPageType<SpellPage> SPELL = BookPageTypeRegistry.register(SpellPage.ID, SpellPage.CODEC, SpellPage.STREAM_CODEC);
  public static final BookPageType<StaffTypePage> STAFF_TYPE =
      BookPageTypeRegistry.register(StaffTypePage.ID, StaffTypePage.CODEC, StaffTypePage.STREAM_CODEC);

  private GuideBookPages() {}

  public static void register() {}

  static <T> Optional<T> lookup(Level level, ResourceKey<? extends Registry<T>> registry, ResourceKey<T> key) {
    Optional<T> value = level.registryAccess().lookupOrThrow(registry).get(key).map(holder -> holder.value());
    if (value.isEmpty()) {
      ArcaneArtistry.LOGGER.warn("Guide book page refers to unknown {}", key);
    }
    return value;
  }

  static BookTextHolder title(Book book, Component title) {
    return new BookTextHolder(title.copy().withStyle(Style.EMPTY.withBold(true).withColor(book.themeData().palette().defaultTitleColor())));
  }

  static BookTextHolder markdown(BookTextRenderer textRenderer, String markdown) {
    return new RenderedBookTextHolder(new MarkdownSource(markdown), textRenderer.render(markdown));
  }

  private static final class MarkdownSource extends BookTextHolder {
    private final String markdown;

    MarkdownSource(String markdown) {
      this.markdown = markdown;
    }

    @Override
    public String getString() {
      return markdown;
    }

    @Override
    public String getKey() {
      return null;
    }

    @Override
    public boolean isEmpty() {
      return markdown.isEmpty();
    }
  }

  static String paragraphs(String first, String second) {
    return first.isEmpty() ? second : second.isEmpty() ? first : first + "\n\n" + second;
  }
}
