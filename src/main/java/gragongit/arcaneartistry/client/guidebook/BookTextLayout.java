package gragongit.arcaneartistry.client.guidebook;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Turns the lightweight markup used in book texts into styled, wrapped lines.
 *
 * <ul>
 * <li>{@code **bold**} and {@code *italic*}</li>
 * <li>{@code {#rrggbb}colored{}}</li>
 * <li>{@code [label](entry:namespace:path)} links to another entry</li>
 * <li>a line break starts a new line</li>
 * </ul>
 */
public final class BookTextLayout {
  public static final int TEXT_COLOR = 0xFF3B2A1A;
  public static final int TITLE_COLOR = 0xFF5A1E0E;
  private static final int LINK_COLOR = 0x1F4E9C;
  private static final String ENTRY_LINK_PREFIX = "entry:";

  private BookTextLayout() {}

  public static List<FormattedText> wrap(Font font, Component text, int width) {
    return font.getSplitter().splitLines(parse(text.getString()), width, Style.EMPTY);
  }

  public static Component parse(String markup) {
    MutableComponent result = Component.empty();
    StringBuilder segment = new StringBuilder();
    boolean bold = false;
    boolean italic = false;
    Integer color = null;
    int i = 0;
    while (i < markup.length()) {
      char c = markup.charAt(i);
      if (markup.startsWith("**", i)) {
        flush(result, segment, style(bold, italic, color));
        bold = !bold;
        i += 2;
      } else if (c == '*') {
        flush(result, segment, style(bold, italic, color));
        italic = !italic;
        i++;
      } else if (markup.startsWith("{}", i)) {
        flush(result, segment, style(bold, italic, color));
        color = null;
        i += 2;
      } else if (c == '{' && i + 8 < markup.length() && markup.charAt(i + 1) == '#' && markup.charAt(i + 8) == '}'
          && parseColor(markup.substring(i + 2, i + 8)) != null) {
        flush(result, segment, style(bold, italic, color));
        color = parseColor(markup.substring(i + 2, i + 8));
        i += 9;
      } else if (c == '[' && parseLink(markup, i) instanceof Link link) {
        flush(result, segment, style(bold, italic, color));
        result.append(Component.literal(link.label()).withStyle(linkStyle(link.target(), bold, italic)));
        i = link.end();
      } else {
        segment.append(c);
        i++;
      }
    }
    flush(result, segment, style(bold, italic, color));
    return result;
  }

  /** The entry a link style points to, see {@link #styleAt}. */
  public static Optional<ResourceKey<BookEntry>> linkTarget(Style style) {
    String insertion = style.getInsertion();
    if (insertion == null || !insertion.startsWith(ENTRY_LINK_PREFIX)) {
      return Optional.empty();
    }
    return Optional
        .ofNullable(Identifier.tryParse(insertion.substring(ENTRY_LINK_PREFIX.length())))
        .map(id -> ResourceKey.create(ModRegistries.BOOK_ENTRY_KEY, id));
  }

  /** The style of the character at {@code x} pixels into {@code line}. */
  public static Optional<Style> styleAt(Font font, FormattedText line, int x) {
    if (x < 0) {
      return Optional.empty();
    }
    AtomicInteger offset = new AtomicInteger();
    return line.visit((style, text) -> {
      int width = font.width(FormattedText.of(text, style));
      if (x < offset.addAndGet(width)) {
        return Optional.of(style);
      }
      return Optional.empty();
    }, Style.EMPTY);
  }

  private record Link(String label, String target, int end) {
  }

  private static Link parseLink(String markup, int start) {
    int labelEnd = markup.indexOf(']', start);
    if (labelEnd < 0 || labelEnd + 1 >= markup.length() || markup.charAt(labelEnd + 1) != '(') {
      return null;
    }
    int targetEnd = markup.indexOf(')', labelEnd);
    if (targetEnd < 0) {
      return null;
    }
    String target = markup.substring(labelEnd + 2, targetEnd);
    if (!target.startsWith(ENTRY_LINK_PREFIX)) {
      return null;
    }
    return new Link(markup.substring(start + 1, labelEnd), target, targetEnd + 1);
  }

  private static Integer parseColor(String hex) {
    try {
      return Integer.parseInt(hex, 16);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Style style(boolean bold, boolean italic, Integer color) {
    Style style = Style.EMPTY.withBold(bold).withItalic(italic);
    return color != null ? style.withColor(color) : style;
  }

  private static Style linkStyle(String target, boolean bold, boolean italic) {
    return style(bold, italic, LINK_COLOR).withUnderlined(true).withInsertion(target);
  }

  private static void flush(MutableComponent result, StringBuilder segment, Style style) {
    if (!segment.isEmpty()) {
      result.append(Component.literal(segment.toString()).withStyle(style));
      segment.setLength(0);
    }
  }
}
