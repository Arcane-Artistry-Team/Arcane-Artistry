package gragongit.arcaneartistry.client.guidebook.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import gragongit.arcaneartistry.client.guidebook.BookTextLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

/**
 * One physical page of the open book. A {@link PageContent} that doesn't fit on one page continues on further display pages.
 *
 * @param heading the entry title, only on the first page of an entry
 */
public record DisplayPage(Optional<Component> heading, Optional<Component> title, Optional<PageVisual> visual, List<FormattedText> lines) {
  private static final int HEADING_HEIGHT = 16;
  private static final int TITLE_HEIGHT = 13;
  private static final int VISUAL_GAP = 4;
  private static final int SEPARATOR_COLOR = 0x805A1E0E;

  public static List<DisplayPage> paginate(Font font, PageContent content, Optional<Component> heading, int width, int height) {
    List<FormattedText> lines = content.text().map(text -> BookTextLayout.wrap(font, text, width)).orElse(List.of());
    List<DisplayPage> pages = new ArrayList<>();
    int headerHeight = (heading.isPresent() ? HEADING_HEIGHT : 0) + (content.title().isPresent() ? TITLE_HEIGHT : 0)
        + content.visual().map(visual -> visual.height() + VISUAL_GAP).orElse(0);
    int taken = Math.min(lines.size(), Math.max(0, (height - headerHeight) / font.lineHeight));
    pages.add(new DisplayPage(heading, content.title(), content.visual(), lines.subList(0, taken)));

    int perPage = Math.max(1, height / font.lineHeight);
    while (taken < lines.size()) {
      while (taken < lines.size() && lines.get(taken).getString().isBlank()) {
        taken++;
      }
      int end = Math.min(lines.size(), taken + perPage);
      if (taken < end) {
        pages.add(new DisplayPage(Optional.empty(), Optional.empty(), Optional.empty(), lines.subList(taken, end)));
      }
      taken = end;
    }
    return pages;
  }

  public void extract(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int mouseX, int mouseY) {
    int top = y;
    if (heading.isPresent()) {
      Component text = heading.get().copy().withStyle(Style.EMPTY.withBold(true));
      graphics.text(font, text, x + (width - font.width(text)) / 2, top, BookTextLayout.TITLE_COLOR, false);
      graphics.fill(x + 8, top + font.lineHeight + 2, x + width - 8, top + font.lineHeight + 3, SEPARATOR_COLOR);
      top += HEADING_HEIGHT;
    }
    if (title.isPresent()) {
      graphics.text(font, title.get(), x + (width - font.width(title.get())) / 2, top, BookTextLayout.TITLE_COLOR, false);
      top += TITLE_HEIGHT;
    }
    if (visual.isPresent()) {
      visual.get().extract(graphics, font, x, top, width, mouseX, mouseY);
      top += visual.get().height() + VISUAL_GAP;
    }
    for (FormattedText line : lines) {
      graphics.text(font, Language.getInstance().getVisualOrder(line), x, top, BookTextLayout.TEXT_COLOR, false);
      top += font.lineHeight;
    }
  }

  /** The text style under the mouse, used to find clicked links. */
  public Optional<Style> styleAt(Font font, int x, int y, int mouseX, int mouseY) {
    int top = y + (heading.isPresent() ? HEADING_HEIGHT : 0) + (title.isPresent() ? TITLE_HEIGHT : 0)
        + visual.map(visual -> visual.height() + VISUAL_GAP).orElse(0);
    if (mouseY < top) {
      return Optional.empty();
    }
    int index = (mouseY - top) / font.lineHeight;
    if (index >= lines.size()) {
      return Optional.empty();
    }
    return BookTextLayout.styleAt(font, lines.get(index), mouseX - x);
  }
}
