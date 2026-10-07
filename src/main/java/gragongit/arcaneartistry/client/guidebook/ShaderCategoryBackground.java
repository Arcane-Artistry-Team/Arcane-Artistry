package gragongit.arcaneartistry.client.guidebook;

import java.util.Optional;
import com.klikli_dev.modonomicon.client.gui.book.node.BookCategoryNodeScreen;
import gragongit.arcaneartistry.client.crystalball.CrystalBallBackground;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class ShaderCategoryBackground {
  private static final String PREFIX = "shaders/";
  private static final String SUFFIX = ".fsh";
  private static final double RADIUS = 300;
  private static final double PARALLAX = 0.5;

  private ShaderCategoryBackground() {}

  public static Optional<Identifier> shaderOf(Identifier background) {
    String path = background.getPath();
    if (!path.startsWith(PREFIX) || !path.endsWith(SUFFIX)) {
      return Optional.empty();
    }
    return Optional.of(background.withPath(path.substring(PREFIX.length(), path.length() - SUFFIX.length())));
  }

  public static void render(GuiGraphicsExtractor graphics, BookCategoryNodeScreen screen, Identifier shader, float scrollX, float scrollY,
      float seconds) {
    int x0 = screen.getInnerX();
    int y0 = screen.getInnerY();
    int x1 = x0 + screen.getInnerWidth();
    int y1 = y0 + screen.getInnerHeight();
    double zoom = screen.getCurrentZoom();
    double rootX = (x0 + x1) / 2.0 - scrollX / 2 * zoom * PARALLAX;
    double rootY = (y0 + y1) / 2.0 - scrollY / 2 * zoom * PARALLAX;
    CrystalBallBackground
        .render(graphics, shader, x0, y0, x1, y1, rootX, rootY, Math.sqrt(zoom), RADIUS, seconds, rootX, rootY,
            CrystalBallBackground.NO_REVEAL_MASK);
  }
}
