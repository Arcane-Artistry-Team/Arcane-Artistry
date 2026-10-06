package gragongit.arcaneartistry.client.guidebook.page;

import gragongit.arcaneartistry.common.guidebook.page.ShowcasePage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

/** A texture scaled to fit the page width, keeping its aspect ratio. */
final class ImageVisual implements PageVisual {
  private static final int MAX_HEIGHT = 100;
  private static final int PADDING = 2;

  private final ShowcasePage.Image image;
  private final int width;
  private final int height;

  ImageVisual(ShowcasePage.Image image, int maxWidth) {
    this.image = image;
    double scale = Math.min(1, Math.min((double) maxWidth / image.width(), (double) MAX_HEIGHT / image.height()));
    this.width = Math.max(1, (int) Math.round(image.width() * scale));
    this.height = Math.max(1, (int) Math.round(image.height() * scale));
  }

  @Override
  public int height() {
    return height + 2 * PADDING;
  }

  @Override
  public void extract(GuiGraphicsExtractor graphics, Font font, int x, int y, int maxWidth, int mouseX, int mouseY) {
    graphics
        .blit(RenderPipelines.GUI_TEXTURED, image.texture(), x + (maxWidth - width) / 2, y + PADDING, 0, 0, width, height, image.width(),
            image.height(), image.width(), image.height());
  }
}
