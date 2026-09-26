package gragongit.arcaneartistry.client.crystalball;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CrystalBallScreen extends Screen {
  private static final int MARGIN = 16;

  private final CrystalBallNode root = CrystalBallNode.createRoot();
  private final CrystalBallCamera camera = new CrystalBallCamera();
  private final CrystalBallRenderer renderer = new CrystalBallRenderer();
  private final CrystalBallRenderer.ChrystalBallNodeStateProvider states;
  private final long openedAt = System.nanoTime();

  public CrystalBallScreen(CrystalBallRenderer.ChrystalBallNodeStateProvider states) {
    super(Component.translatable("screen.arcane_artistry.crystal_ball"));
    this.states = states;
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    super.extractRenderState(graphics, mouseX, mouseY, delta);
    int x0 = MARGIN;
    int y0 = MARGIN;
    int x1 = this.width - MARGIN;
    int y1 = this.height - MARGIN;
    float rootX = (x0 + x1) / 2f + camera.panX();
    float rootY = (y0 + y1) / 2f + camera.panY();
    CrystalBallGalaxy.render(graphics, x0, y0, x1, y1, rootX, rootY, camera.zoom(), (System.nanoTime() - openedAt) / 1_000_000_000f);
    renderer.render(graphics, this.font, root, camera, states, x0, y0, x1, y1);
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    camera.drag((float) dx, (float) dy);
    return true;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    camera.zoomAt((float) (mouseX - this.width / 2), (float) (mouseY - this.height / 2), (float) Math.pow(1.25, scrollY));
    return true;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
