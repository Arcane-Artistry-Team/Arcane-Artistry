package gragongit.arcaneartistry.client.guidebook;

import java.util.Optional;
import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.client.gui.book.markdown.BookTextRenderer;
import com.klikli_dev.modonomicon.data.BookDataManager;
import gragongit.arcaneartistry.common.guidebook.GuideBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class GuideBookMarkdown {
  private GuideBookMarkdown() {}

  public static Component render(Component text, Style style) {
    Book book = BookDataManager.get().getBook(GuideBook.ID);
    ClientLevel level = Minecraft.getInstance().level;
    if (book == null || level == null) {
      return ComponentUtils.mergeStyles(text, style);
    }
    MutableComponent result = Component.empty();
    for (MutableComponent paragraph : new BookTextRenderer(book, level.registryAccess()).render(text.getString(), style)) {
      paragraph.visit((partStyle, part) -> {
        result.append(Component.literal(part).withStyle(partStyle.withFont(FontDescription.DEFAULT)));
        return Optional.empty();
      }, Style.EMPTY);
    }
    return result;
  }
}
