package gragongit.arcaneartistry.client.crystalball;

import java.util.Optional;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.crystalball.CrystalBallEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class CrystalBallScreen extends Screen {
  private static final int MARGIN = 16;
  private static final int HOME_BUTTON_WIDTH = 50;
  private static final int HOME_BUTTON_HEIGHT = 20;
  private static final int BUTTON_PADDING = 4;

  private static final float CLICK_FOCUS_SECONDS = 1f;
  private static final double CLICK_DRAG_TOLERANCE = 3;

  private static final float INTRO_SECONDS = 2f;
  private static final int INTRO_DEPTHS = 3;

  private static final double PAN_MARGIN = 24;
  private static final double STAGGER_MIN_DEPTHS = 1.01;

  private static final float HOVER_FADE_IN = 0.06f;
  private static final float HOVER_FADE_OUT = 0.12f;
  private static final float HOVER_MAX_FADE = 0.3f;

  private final CrystalBallNode root = CrystalBallNode.createRoot();
  private final CrystalBallCamera camera = new CrystalBallCamera();
  private final CrystalBallRenderer renderer;
  private final CrystalBallNodeHover hover = new CrystalBallNodeHover();
  private final int maxDepth;
  private final double backgroundRadius;
  private final CrystalBallRenderer.NodeStateProvider states;
  private final long openedAt = System.nanoTime();
  private double rootFocusZoom;
  private double deepestFocusZoom;
  private boolean initialized;
  private boolean clickPending;
  private double clickDragDistance;
  private boolean hovering;
  private float hoverFade;

  public CrystalBallScreen(CrystalBallRenderer.NodeStateProvider states, int maxDepth) {
    super(Component.translatable("screen.arcane_artistry.crystal_ball"));
    this.states = states;
    this.maxDepth = maxDepth;
    this.backgroundRadius = CrystalBallRenderer.treeRadius(maxDepth);
    this.renderer = new CrystalBallRenderer(maxDepth);
  }

  @Override
  protected void init() {
    rootFocusZoom = CrystalBallRenderer.focusZoom(0, viewWidth(), viewHeight());
    deepestFocusZoom = CrystalBallRenderer.focusZoom(maxDepth, viewWidth(), viewHeight());
    camera.setZoomRange(rootFocusZoom, deepestFocusZoom);
    camera.setFocusBounds((x, y, zoom) -> CrystalBallRenderer.clampFocus(x, y, zoom, maxDepth, viewWidth(), viewHeight(), PAN_MARGIN));
    if (!initialized) {
      initialized = true;
      camera.jumpTo(0, 0, rootFocusZoom / Math.pow(CrystalBallRenderer.depthZoomFactor(), INTRO_DEPTHS));
      focus(CastPattern.empty(), INTRO_SECONDS);
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
    boolean staggered = CrystalBallRenderer.depthsBetween(camera.zoom(), zoom) > STAGGER_MIN_DEPTHS;
    camera.flyTo(target.x(), target.y(), zoom, seconds, staggered);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    camera.update();
    int x0 = MARGIN;
    int y0 = MARGIN;
    int x1 = this.width - MARGIN;
    int y1 = this.height - MARGIN;
    double backgroundZoom = CrystalBallBackground.parallaxZoom(camera.zoom(), rootFocusZoom, deepestFocusZoom);
    double backgroundPan = backgroundZoom / camera.zoom();
    double rootX = (x0 + x1) / 2.0 + camera.panX() * backgroundPan;
    double rootY = (y0 + y1) / 2.0 + camera.panY() * backgroundPan;
    CrystalBallBackground
        .render(graphics, states.backgroundShader().orElse(CrystalBallBackground.DEFAULT_SHADER), x0, y0, x1, y1, rootX, rootY,
            backgroundZoom, backgroundRadius, (System.nanoTime() - openedAt) / 1_000_000_000f);
    renderer.render(graphics, this.font, root, camera, states, x0, y0, x1, y1);
    if (renderer.nodeAt(mouseX, mouseY).isPresent()) {
      graphics.requestCursor(CursorTypes.POINTING_HAND);
    }
    Optional<CrystalBallRenderer.HoveredNode> hovered = camera.isFlying() ? Optional.empty()
        : renderer.hoveredNode(mouseX, mouseY).filter(node -> entryOf(node).isPresent());
    hovering = hovered.isPresent();
    if (hoverFade > 0) {
      graphics.fill(x0, y0, x1, y1, Mth.floor(hoverFade * 255) << 24);
    }
    super.extractRenderState(graphics, mouseX, mouseY, delta);
    hovered.ifPresent(node -> {
      graphics.nextStratum();
      hover.extract(graphics, this.font, entryOf(node).orElseThrow(), states.stateOf(node.node()), node, y1, this.width);
    });
  }

  private Optional<CrystalBallEntry> entryOf(CrystalBallRenderer.HoveredNode node) {
    CrystalBallNodeState state = states.stateOf(node.node());
    if (state != CrystalBallNodeState.ROOT && state != CrystalBallNodeState.SPELL) {
      return Optional.empty();
    }
    return states.entryOf(node.node());
  }

  @Override
  public void tick() {
    if (hovering) {
      hoverFade = Math.clamp(hoverFade + HOVER_FADE_IN, 0, HOVER_MAX_FADE);
    } else {
      hoverFade = Math.clamp(hoverFade - HOVER_FADE_OUT, 0, 1);
    }
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (super.mouseClicked(event, doubleClick)) {
      return true;
    }
    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
      clickPending = true;
      clickDragDistance = 0;
    }
    return true;
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    boolean handled = super.mouseReleased(event);
    setFocused(null);
    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && clickPending) {
      clickPending = false;
      if (clickDragDistance <= CLICK_DRAG_TOLERANCE) {
        renderer.nodeAt(event.x(), event.y()).ifPresent(node -> focus(node.path(), CLICK_FOCUS_SECONDS));
        return true;
      }
    }
    return handled;
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    clickDragDistance += Math.abs(dx) + Math.abs(dy);
    if (!camera.isFlying()) {
      camera.drag(dx, dy);
    }
    return true;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (camera.isFlying()) {
      return true;
    }
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
