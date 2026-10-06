package gragongit.arcaneartistry.client.guidebook.page;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** The non-text part of a page, e.g. a recipe or a showcased item, drawn above the page text. */
public interface PageVisual {
  int height();

  void extract(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int mouseX, int mouseY);
}
