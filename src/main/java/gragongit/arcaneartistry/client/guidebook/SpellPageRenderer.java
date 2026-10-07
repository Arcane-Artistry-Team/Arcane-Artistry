package gragongit.arcaneartistry.client.guidebook;

import org.jspecify.annotations.Nullable;
import com.klikli_dev.modonomicon.client.gui.book.entry.BookEntryScreen;
import com.klikli_dev.modonomicon.client.render.page.BookPageRenderer;
import com.klikli_dev.modonomicon.client.render.page.PageWithTextRenderer;
import gragongit.arcaneartistry.common.guidebook.SpellPage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Style;

public class SpellPageRenderer extends BookPageRenderer<SpellPage> implements PageWithTextRenderer {
  private static final int SLOT_WIDTH = 66;
  private static final int SLOT_Y = 10;
  private static final int ICON_SIZE = 16;
  private static final int ICON_X = BookEntryScreen.PAGE_WIDTH / 2 - ICON_SIZE / 2;
  private static final int ICON_Y = 15;
  private static final int TEXT_Y = 40;

  public SpellPageRenderer(SpellPage page) {
    super(page);
  }

  @Override
  public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float ticks) {
    if (page.hasTitle()) {
      renderTitle(graphics, page.getTitle(), false, BookEntryScreen.PAGE_WIDTH / 2, 0);
    }
    page.getBook().theme().content().spotlightSlot().extractRenderState(graphics, BookEntryScreen.PAGE_WIDTH / 2 - SLOT_WIDTH / 2, SLOT_Y);
    page
        .icon()
        .ifPresent(icon -> graphics
            .blit(RenderPipelines.GUI_TEXTURED, icon, ICON_X, ICON_Y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, -1));
    renderBookTextHolder(graphics, page.getText(), 0, TEXT_Y, BookEntryScreen.PAGE_WIDTH, BookEntryScreen.PAGE_HEIGHT - TEXT_Y);

    Style style = getClickedComponentStyleAt(mouseX, mouseY);
    if (style != null) {
      parentScreen.renderComponentHoverEffect(graphics, style, mouseX + parentScreen.getBookLeft() + left,
          mouseY + parentScreen.getBookTop() + top);
    }
  }

  @Override
  public @Nullable Style getClickedComponentStyleAt(double mouseX, double mouseY) {
    if (mouseX > 0 && mouseY > 0) {
      if (page.hasTitle()) {
        Style titleStyle = getClickedComponentStyleAtForTitle(page.getTitle(), BookEntryScreen.PAGE_WIDTH / 2, 0, mouseX, mouseY);
        if (titleStyle != null) {
          return titleStyle;
        }
      }
      var bounds = getBookTextHolderBounds(0, TEXT_Y, BookEntryScreen.PAGE_WIDTH, BookEntryScreen.PAGE_HEIGHT - TEXT_Y);
      Style textStyle =
          getClickedComponentStyleAtForTextHolder(page.getText(), bounds.x, bounds.y, bounds.width, bounds.height, mouseX, mouseY);
      if (textStyle != null) {
        return textStyle;
      }
    }
    return super.getClickedComponentStyleAt(mouseX, mouseY);
  }

  @Override
  public int getTextY() {
    return TEXT_Y;
  }
}
