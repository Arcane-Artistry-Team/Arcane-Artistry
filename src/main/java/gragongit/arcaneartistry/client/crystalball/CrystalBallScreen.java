package gragongit.arcaneartistry.client.crystalball;

import java.util.Optional;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import gragongit.arcaneartistry.client.gui.MapCamera;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.crystalball.CrystalBallEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CrystalBallScreen extends Screen {
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

  private static final float REVEAL_SECONDS = 0.6f;
  private static final float CLOSE_SECONDS = 0.4f;
  private static final double REVEAL_RIM_WIDTH = 8;

  private final CrystalBallNode root = CrystalBallNode.createRoot();
  private final MapCamera camera = new MapCamera();
  private final CrystalBallRenderer renderer;
  private final CrystalBallNodeHover hover = new CrystalBallNodeHover();
  private final int maxDepth;
  private final double backgroundRadius;
  private final CrystalBallRenderer.NodeStateProvider states;
  private final long openedAt = System.nanoTime();
  private final Vec3 revealOrigin;
  private double revealX;
  private double revealY;
  private double revealMaxRadius;
  private long closingAt;
  private float closingFrom;
  private boolean closing;
  private boolean introStarted;
  private double rootFocusZoom;
  private double deepestFocusZoom;
  private boolean initialized;
  private boolean clickPending;
  private double clickDragDistance;
  private boolean hovering;
  private float hoverFade;

  public CrystalBallScreen(CrystalBallRenderer.NodeStateProvider states, int maxDepth, Vec3 revealOrigin) {
    super(Component.translatable("screen.arcane-artistry.crystal_ball"));
    this.states = states;
    this.revealOrigin = revealOrigin;
    this.maxDepth = maxDepth;
    this.backgroundRadius = CrystalBallRenderer.treeRadius(maxDepth);
    this.renderer = new CrystalBallRenderer(maxDepth);
  }

  @Override
  protected void init() {
    rootFocusZoom = CrystalBallRenderer.focusZoom(0, this.width, this.height);
    deepestFocusZoom = CrystalBallRenderer.focusZoom(maxDepth, this.width, this.height);
    camera.setZoomRange(rootFocusZoom, deepestFocusZoom);
    camera.setFocusBounds((x, y, zoom) -> CrystalBallRenderer.clampFocus(x, y, zoom, maxDepth, this.width, this.height, PAN_MARGIN));
    if (!initialized) {
      initialized = true;
      camera.jumpTo(0, 0, rootFocusZoom / Math.pow(CrystalBallRenderer.depthZoomFactor(), INTRO_DEPTHS));
    }
    projectRevealOrigin();
    int x = this.width - BUTTON_PADDING - HOME_BUTTON_WIDTH;
    int y = this.height - BUTTON_PADDING - HOME_BUTTON_HEIGHT;
    addRenderableWidget(Button
        .builder(Component.translatable("screen.arcane-artistry.crystal_ball.home"), button -> focus(CastPattern.empty(), 1f))
        .bounds(x, y, HOME_BUTTON_WIDTH, HOME_BUTTON_HEIGHT)
        .build());
  }

  private void projectRevealOrigin() {
    Vec3 projected = this.minecraft.gameRenderer.projectPointToScreen(revealOrigin);
    boolean visible = projected.z > -1 && projected.z < 1;
    revealX = visible ? Math.clamp((projected.x + 1) / 2 * this.width, 0, this.width) : this.width / 2.0;
    revealY = visible ? Math.clamp((1 - projected.y) / 2 * this.height, 0, this.height) : this.height / 2.0;
    double farthestX = Math.max(revealX, this.width - revealX);
    double farthestY = Math.max(revealY, this.height - revealY);
    revealMaxRadius = Math.sqrt(farthestX * farthestX + farthestY * farthestY) + REVEAL_RIM_WIDTH;
  }

  private float revealProgress() {
    if (closing) {
      return Math.max(0, closingFrom - secondsSince(closingAt) / CLOSE_SECONDS);
    }
    return Math.min(1, secondsSince(openedAt) / REVEAL_SECONDS);
  }

  private boolean isRevealed() {
    return !closing && revealProgress() >= 1;
  }

  private static float secondsSince(long nanoTime) {
    return (System.nanoTime() - nanoTime) / 1_000_000_000f;
  }

  public void focus(CastPattern pattern, float seconds) {
    MapCamera.Position target = CrystalBallRenderer.worldPositionOf(pattern);
    double zoom = CrystalBallRenderer.focusZoom(pattern.size(), this.width, this.height);
    boolean staggered = CrystalBallRenderer.depthsBetween(camera.zoom(), zoom) > STAGGER_MIN_DEPTHS;
    camera.flyTo(target.x(), target.y(), zoom, seconds, staggered);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    boolean revealed = isRevealed();
    if (revealed && !introStarted) {
      introStarted = true;
      focus(CastPattern.empty(), INTRO_SECONDS);
    }
    camera.update();
    int x0 = 0;
    int y0 = 0;
    int x1 = this.width;
    int y1 = this.height;
    double backgroundZoom = CrystalBallBackground.parallaxZoom(camera.zoom(), rootFocusZoom, deepestFocusZoom);
    double backgroundPan = backgroundZoom / camera.zoom();
    double rootX = (x0 + x1) / 2.0 + camera.panX() * backgroundPan;
    double rootY = (y0 + y1) / 2.0 + camera.panY() * backgroundPan;
    float easedReveal = 1 - (float) Math.pow(1 - revealProgress(), 3);
    double revealRadius = revealed ? CrystalBallBackground.NO_REVEAL_MASK : easedReveal * revealMaxRadius;
    CrystalBallBackground
        .render(graphics, states.backgroundShader().orElse(CrystalBallBackground.DEFAULT_SHADER), x0, y0, x1, y1, rootX, rootY,
            backgroundZoom, backgroundRadius, secondsSince(openedAt), revealX, revealY, revealRadius);
    if (!revealed) {
      return;
    }

    renderer.render(graphics, this.font, root, camera, states, x0, y0, x1, y1);
    if (renderer.nodeAt(mouseX, mouseY).isPresent()) {
      graphics.requestCursor(CursorTypes.POINTING_HAND);
    }
    Optional<CrystalBallRenderer.HoveredNode> hovered =
        camera.isFlying() ? Optional.empty() : renderer.hoveredNode(mouseX, mouseY).filter(node -> entryOf(node).isPresent());
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
  public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {}

  @Override
  public void onClose() {
    if (!closing) {
      closingFrom = revealProgress();
      closingAt = System.nanoTime();
      closing = true;
    }
  }

  @Override
  public void tick() {
    if (closing && revealProgress() <= 0) {
      super.onClose();
      return;
    }

    if (hovering) {
      hoverFade = Math.clamp(hoverFade + HOVER_FADE_IN, 0, HOVER_MAX_FADE);
    } else {
      hoverFade = Math.clamp(hoverFade - HOVER_FADE_OUT, 0, 1);
    }
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    if (super.keyPressed(event)) {
      return true;
    }
    if (this.minecraft.options.keyInventory.matches(event)) {
      onClose();
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (!isRevealed()) {
      return true;
    }
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
    if (!isRevealed()) {
      return true;
    }
    clickDragDistance += Math.abs(dx) + Math.abs(dy);
    if (!camera.isFlying()) {
      camera.drag(dx, dy);
    }
    return true;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (!isRevealed() || camera.isFlying()) {
      return true;
    }
    double targetZoom = CrystalBallRenderer.steppedZoom(camera.zoom(), scrollY, this.width, this.height);
    camera.zoomAt(mouseX - this.width / 2, mouseY - this.height / 2, targetZoom / camera.zoom());
    return true;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
