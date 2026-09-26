package gragongit.arcaneartistry.client.crystalball;

import gragongit.arcaneartistry.common.api.CastPattern;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class CrystalBallScreen extends Screen {
  private static final int MARGIN = 16;
  private static final int BUTTON_PADDING = 4;
  private static final int HOME_BUTTON_WIDTH = 50;
  private static final int HOME_BUTTON_HEIGHT = 20;

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
  protected void init() {
    int x = this.width - MARGIN - BUTTON_PADDING - HOME_BUTTON_WIDTH;
    int y = this.height - MARGIN - BUTTON_PADDING - HOME_BUTTON_HEIGHT;
    addRenderableWidget(Button
        .builder(Component.translatable("screen.arcane_artistry.crystal_ball.home"), button -> focus(CastPattern.empty(), 1f))
        .bounds(x, y, HOME_BUTTON_WIDTH, HOME_BUTTON_HEIGHT)
        .build());
  }

  public void focus(CastPattern pattern, float seconds) {
    Vec2 target = CrystalBallRenderer.worldPositionOf(pattern);
    float zoom = CrystalBallRenderer.focusZoom(pattern.strokes().size(), this.width - 2 * MARGIN, this.height - 2 * MARGIN);
    camera.flyTo(target.x, target.y, zoom, seconds);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    camera.update();
    int x0 = MARGIN;
    int y0 = MARGIN;
    int x1 = this.width - MARGIN;
    int y1 = this.height - MARGIN;
    float rootX = (x0 + x1) / 2f + camera.panX();
    float rootY = (y0 + y1) / 2f + camera.panY();
    CrystalBallGalaxy.render(graphics, x0, y0, x1, y1, rootX, rootY, camera.zoom(), (System.nanoTime() - openedAt) / 1_000_000_000f);
    renderer.render(graphics, this.font, root, camera, states, x0, y0, x1, y1);
    super.extractRenderState(graphics, mouseX, mouseY, delta);
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
