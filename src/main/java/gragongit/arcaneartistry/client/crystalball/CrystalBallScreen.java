package gragongit.arcaneartistry.client.crystalball;

import com.mojang.blaze3d.platform.InputConstants;
import gragongit.arcaneartistry.common.api.CastPattern;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CrystalBallScreen extends Screen {
  private static final int MARGIN = 16;
  private static final int BUTTON_PADDING = 4;
  private static final int HOME_BUTTON_WIDTH = 50;
  private static final int HOME_BUTTON_HEIGHT = 20;
  private static final int LEFT_BUTTON = InputConstants.MOUSE_BUTTON_LEFT;
  private static final double CLICK_DRAG_TOLERANCE = 3;
  private static final float CLICK_FOCUS_SECONDS = 1f;

  private final CrystalBallNode root = CrystalBallNode.createRoot();
  private final CrystalBallCamera camera = new CrystalBallCamera();
  private final CrystalBallRenderer renderer;
  private final int maxDepth;
  private final double galaxyRadius;
  private final CrystalBallRenderer.ChrystalBallNodeStateProvider states;
  private final long openedAt = System.nanoTime();
  private double rootFocusZoom;
  private double deepestFocusZoom;
  private boolean initialized;
  private boolean clickPending;
  private double clickDragDistance;

  public CrystalBallScreen(CrystalBallRenderer.ChrystalBallNodeStateProvider states, int maxDepth) {
    super(Component.translatable("screen.arcane_artistry.crystal_ball"));
    this.states = states;
    this.maxDepth = maxDepth;
    this.galaxyRadius = CrystalBallRenderer.treeRadius(maxDepth);
    this.renderer = new CrystalBallRenderer(maxDepth);
  }

  @Override
  protected void init() {
    camera.setMaxZoom(CrystalBallRenderer.maxZoom(maxDepth, viewWidth(), viewHeight()));
    rootFocusZoom = CrystalBallRenderer.focusZoom(0, viewWidth(), viewHeight());
    deepestFocusZoom = CrystalBallRenderer.focusZoom(maxDepth, viewWidth(), viewHeight());
    if (!initialized) {
      initialized = true;
      focus(CastPattern.empty(), 0);
    }
    int x = this.width - MARGIN - BUTTON_PADDING - HOME_BUTTON_WIDTH;
    int y = this.height - MARGIN - BUTTON_PADDING - HOME_BUTTON_HEIGHT;
    addRenderableWidget(Button
        .builder(Component.translatable("screen.arcane_artistry.crystal_ball.home"), button -> focus(CastPattern.empty(), 1f))
        .bounds(x, y, HOME_BUTTON_WIDTH, HOME_BUTTON_HEIGHT)
        .build());
  }

  public void focus(CastPattern pattern, float seconds) {
    CrystalBallRenderer.WorldPosition target = CrystalBallRenderer.worldPositionOf(pattern);
    double zoom = CrystalBallRenderer.focusZoom(pattern.size(), viewWidth(), viewHeight());
    camera.flyTo(target.x(), target.y(), zoom, seconds);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    camera.update();
    int x0 = MARGIN;
    int y0 = MARGIN;
    int x1 = this.width - MARGIN;
    int y1 = this.height - MARGIN;
    double galaxyZoom = CrystalBallGalaxy.parallaxZoom(camera.zoom(), rootFocusZoom, deepestFocusZoom);
    double galaxyPan = galaxyZoom / camera.zoom();
    double rootX = (x0 + x1) / 2.0 + camera.panX() * galaxyPan;
    double rootY = (y0 + y1) / 2.0 + camera.panY() * galaxyPan;
    CrystalBallGalaxy.render(graphics, x0, y0, x1, y1, rootX, rootY, galaxyZoom, galaxyRadius, (System.nanoTime() - openedAt) / 1_000_000_000f);
    renderer.render(graphics, this.font, root, camera, states, x0, y0, x1, y1);
    super.extractRenderState(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (super.mouseClicked(event, doubleClick)) {
      return true;
    }
    if (event.button() == LEFT_BUTTON) {
      clickPending = true;
      clickDragDistance = 0;
    }
    return true;
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    if (event.button() == LEFT_BUTTON && clickPending) {
      clickPending = false;
      if (clickDragDistance <= CLICK_DRAG_TOLERANCE) {
        renderer.nodeAt(event.x(), event.y()).ifPresent(node -> focus(node.path(), CLICK_FOCUS_SECONDS));
        return true;
      }
    }
    return super.mouseReleased(event);
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    clickDragDistance += Math.abs(dx) + Math.abs(dy);
    camera.drag(dx, dy);
    return true;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    double targetZoom = CrystalBallRenderer.steppedZoom(camera.zoom(), scrollY, viewWidth(), viewHeight());
    camera.zoomAt(mouseX - this.width / 2, mouseY - this.height / 2, targetZoom / camera.zoom());
    return true;
  }

  private int viewWidth() {
    return this.width - 2 * MARGIN;
  }

  private int viewHeight() {
    return this.height - 2 * MARGIN;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
