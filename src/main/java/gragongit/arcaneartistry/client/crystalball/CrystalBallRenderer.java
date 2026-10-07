package gragongit.arcaneartistry.client.crystalball;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.presentation.Presentation;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class CrystalBallRenderer {

  public interface NodeStateProvider {
    CrystalBallNodeState stateOf(CrystalBallNode node);

    int connectionColor();

    default Optional<Identifier> backgroundShader() {
      return Optional.empty();
    }

    default Optional<Presentation> presentationOf(CrystalBallNode node) {
      return Optional.empty();
    }

    default Optional<Identifier> iconOf(CrystalBallNode node) {
      return presentationOf(node).map(Presentation::icon);
    }
  }

  public record WorldPosition(double x, double y) {
  }

  public record HoveredNode(CrystalBallNode node, double screenX, double screenY, float sizePx, float focusSizePx) {
  }

  private record Visible(CrystalBallNode node, double worldX, double worldY, int depth) {
  }

  private record Edge(float ax, float ay, float bx, float by, int parentDepth, float fade) {
  }

  private static final int QUESTION_MARK = 0xA0A0A0;
  private static final int OUTLINE_COLOR = 0x000000;
  private static final float COORD_LIMIT = 1_000_000f;

  private static final int CONNECTION_WIDTH = 2;
  private static final double PULSE_INTERVAL = 2;
  private static final double PULSE_EDGE_DURATION = 1.5;
  private static final float PULSE_TAIL = 0.1f;
  private static final float PULSE_MIN_TAIL = 3;
  private static final float PULSE_HALO = 0.33f;
  private static final float PULSE_HALO_ALPHA = 0.35f;
  private static final float PULSE_SATURATION = 0.7f;
  private static final float PULSE_HEAD_SATURATION = 0.3f;

  private static final int STAR_MIN_SIZE = 1;
  private static final int STAR_MAX_SIZE = 3;
  private static final int BIG_STAR_MIN_SIZE = 5;
  private static final int BIG_STAR_MAX_SIZE = 9;
  private static final float SPARKLE_CHANCE = 0.5f;
  private static final float STAR_HIT_PADDING = 2;
  private static final int[][][] STAR_OUTLINES = starOutlines();

  private static final Identifier FRAME_ROOT = Identifier.withDefaultNamespace("advancements/goal_frame_obtained");
  private static final Identifier FRAME_UNKNOWN = Identifier.withDefaultNamespace("advancements/task_frame_unobtained");
  private static final Identifier FRAME_EXPLORED = Identifier.withDefaultNamespace("advancements/task_frame_obtained");
  private static final Identifier FRAME_SPELL = Identifier.withDefaultNamespace("advancements/challenge_frame_obtained");
  static final float ICON_SIZE_FACTOR = 0.6f;

  private static final float ROOT_SIZE = 26;
  private static final float ROOT_EDGE_LENGTH = ROOT_SIZE * 4;
  private static final double DEPTH_SCALE = 0.45;
  private static final float LANE_FACTOR = 5;

  private static final float FOCUS_FILL = 0.85f;
  private static final int ZOOM_STEPS_PER_DEPTH = 4;
  private static final float MIN_FOCUS_NODE_PX = ROOT_SIZE;

  private static final double ROTATION_MARGIN = 1.25;
  private static final double ROTATION =
      Math.asin(Math.min(1, ROTATION_MARGIN * (LANE_FACTOR / ROOT_SIZE) * (1 + DEPTH_SCALE) * ROOT_SIZE / ROOT_EDGE_LENGTH));
  private static final double[][] EDGE_DIR_X = edgeDirections(true);
  private static final double[][] EDGE_DIR_Y = edgeDirections(false);
  private static final double[] DEPTH_SCALE_POWERS = depthScalePowers(CastPattern.MAX_LENGTH + 1);

  private final int maxDepth;
  private final List<Visible> visible = new ArrayList<>();
  private final List<Edge> edges = new ArrayList<>();
  private final List<Visible> bigStars = new ArrayList<>();
  private final List<Visible> stars = new ArrayList<>();

  private double anchorX;
  private double anchorY;
  private double anchorZoom = Double.NaN;

  private GuiGraphicsExtractor graphics;
  private NodeStateProvider states;
  private int vx0, vy0, vx1, vy1;
  private double zoom;
  private float centerX, centerY;
  private double offsetX, offsetY;
  private double time;
  private int connectionColor, pulseColor, pulseHeadColor;
  private float focusSizePx;
  private float fadeInPx, fadeOutPx, fadeLargeStartPx, fadeLargeMidPx, fadeLargeEndPx;

  public CrystalBallRenderer(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public void render(GuiGraphicsExtractor graphics, Font font, CrystalBallNode root, CrystalBallCamera camera, NodeStateProvider states,
      int x0, int y0, int x1, int y1) {
    this.graphics = graphics;
    this.states = states;
    this.vx0 = x0;
    this.vy0 = y0;
    this.vx1 = x1;
    this.vy1 = y1;
    this.centerX = (x0 + x1) / 2f;
    this.centerY = (y0 + y1) / 2f;
    double focusNodePx = focusNodePx(x1 - x0, y1 - y0);
    this.focusSizePx = (float) focusNodePx;
    this.fadeInPx = (float) (focusNodePx * size(1) / size(0));
    this.fadeOutPx = (float) fadeOutPx(focusNodePx);
    this.fadeLargeStartPx = (float) (focusNodePx * size(0) / size(2));
    this.fadeLargeEndPx = (float) fadeLargeEndPx(focusNodePx);
    this.fadeLargeMidPx = Mth.sqrt(fadeLargeStartPx * fadeLargeEndPx);
    this.zoom = camera.zoom();
    if (zoom != anchorZoom) {
      anchorZoom = zoom;
      anchorX = camera.focusX();
      anchorY = camera.focusY();
    }
    this.offsetX = camera.panX() + anchorX * zoom;
    this.offsetY = camera.panY() + anchorY * zoom;
    this.time = System.nanoTime() / 1_000_000_000.0;
    this.connectionColor = ARGB.opaque(states.connectionColor());
    this.pulseColor = ARGB.opaque(brighter(connectionColor, PULSE_SATURATION));
    this.pulseHeadColor = ARGB.opaque(brighter(connectionColor, PULSE_HEAD_SATURATION));

    visible.clear();
    edges.clear();
    bigStars.clear();
    stars.clear();
    graphics.enableScissor(x0, y0, x1, y1);

    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate((float) offsetX, (float) offsetY);
    collect(root, 0, 0, 0);
    for (Edge e : edges) {
      drawConnection(e, true);
    }
    for (Edge e : edges) {
      drawConnection(e, false);
    }
    for (Visible v : visible) {
      drawNode(font, v);
    }
    drawStars(true);
    drawStars(false);
    pose.popMatrix();
    graphics.disableScissor();

    this.graphics = null;
    this.states = null;
  }

  public Optional<CrystalBallNode> nodeAt(double mouseX, double mouseY) {
    if (!inViewport(mouseX, mouseY)) {
      return Optional.empty();
    }
    Visible node = visibleNodeAt(mouseX, mouseY);
    if (node != null) {
      return Optional.of(node.node());
    }
    Visible nearest = nearestStar(bigStars, mouseX, mouseY, BIG_STAR_MAX_SIZE / 2f + STAR_HIT_PADDING, null);
    nearest = nearestStar(stars, mouseX, mouseY, STAR_MAX_SIZE / 2f + STAR_HIT_PADDING, nearest);
    return Optional.ofNullable(nearest).map(Visible::node);
  }

  public Optional<HoveredNode> hoveredNode(double mouseX, double mouseY) {
    if (!inViewport(mouseX, mouseY)) {
      return Optional.empty();
    }
    return Optional
        .ofNullable(visibleNodeAt(mouseX, mouseY))
        .map(v -> new HoveredNode(v.node(), screenX(v.worldX()), screenY(v.worldY()), sizePx(v.depth()), focusSizePx));
  }

  private boolean inViewport(double mouseX, double mouseY) {
    return mouseX >= vx0 && mouseX <= vx1 && mouseY >= vy0 && mouseY <= vy1;
  }

  private Visible visibleNodeAt(double mouseX, double mouseY) {
    for (int i = visible.size() - 1; i >= 0; i--) {
      Visible v = visible.get(i);
      float sizePx = sizePx(v.depth());
      if (visibility(sizePx) <= 0f || Math.round(sizePx) < 2) {
        continue;
      }
      float half = sizePx / 2;
      if (Math.abs(mouseX - screenX(v.worldX())) <= half && Math.abs(mouseY - screenY(v.worldY())) <= half) {
        return v;
      }
    }
    return null;
  }

  private Visible nearestStar(List<Visible> candidates, double mouseX, double mouseY, float radius, Visible best) {
    double bestDist = best == null ? radius * radius : distSq(best, mouseX, mouseY);
    for (Visible v : candidates) {
      double d = distSq(v, mouseX, mouseY);
      if (d <= radius * radius && d < bestDist) {
        best = v;
        bestDist = d;
      }
    }
    return best;
  }

  private double distSq(Visible v, double mouseX, double mouseY) {
    double dx = mouseX - screenX(v.worldX());
    double dy = mouseY - screenY(v.worldY());
    return dx * dx + dy * dy;
  }

  private void collect(CrystalBallNode node, int depth, double worldX, double worldY) {
    float sizePx = sizePx(depth);
    double sx = screenX(worldX);
    double sy = screenY(worldY);

    double r = subtreeRadius(depth) * zoom;
    if (sx + r < vx0 || sx - r > vx1 || sy + r < vy0 || sy - r > vy1) {
      return;
    }

    if (fadeSmall(sizePx) <= 0f) {
      bigStars.add(new Visible(node, worldX, worldY, depth));
      if (depth < maxDepth) {
        collectSmallStars(node, depth, worldX, worldY);
      }
      return;
    }

    float half = sizePx / 2;
    if (sx + half >= vx0 && sx - half <= vx1 && sy + half >= vy0 && sy - half <= vy1) {
      visible.add(new Visible(node, worldX, worldY, depth));
    }

    if (depth >= maxDepth) {
      return;
    }
    for (StaffDirection dir : StaffDirection.values()) {
      int childDepth = depth + 1;
      double length = edgeLength(childDepth);
      double childWorldX = worldX + edgeDirX(dir, depth) * length;
      double childWorldY = worldY + edgeDirY(dir, depth) * length;
      CrystalBallNode child = node.child(dir);
      drawEdge(depth, worldX, worldY, child, childDepth, childWorldX, childWorldY, dir);
      collect(child, childDepth, childWorldX, childWorldY);
    }
  }

  private void collectSmallStars(CrystalBallNode node, int depth, double worldX, double worldY) {
    int childDepth = depth + 1;
    double length = edgeLength(childDepth);
    for (StaffDirection dir : StaffDirection.values()) {
      stars.add(new Visible(node.child(dir), worldX + edgeDirX(dir, depth) * length, worldY + edgeDirY(dir, depth) * length, childDepth));
    }
  }

  public static WorldPosition worldPositionOf(CastPattern pattern) {
    double x = 0;
    double y = 0;
    for (int depth = 0; depth < pattern.size(); depth++) {
      StaffDirection dir = pattern.get(depth);
      double length = edgeLength(depth + 1);
      x += edgeDirX(dir, depth) * length;
      y += edgeDirY(dir, depth) * length;
    }
    return new WorldPosition(x, y);
  }

  public static double treeRadius(int maxDepth) {
    double radius = 0;
    for (StaffDirection dir : StaffDirection.values()) {
      CastPattern straight = CastPattern.empty();
      for (int i = 0; i < maxDepth; i++) {
        straight = straight.add(dir);
      }
      WorldPosition end = worldPositionOf(straight);
      radius = Math.max(radius, Math.hypot(end.x(), end.y()));
    }
    return radius;
  }

  public static double depthZoomFactor() {
    return size(0) / size(1);
  }

  public static double depthsBetween(double zoomA, double zoomB) {
    return Math.abs(Math.log(zoomB / zoomA)) / Math.log(depthZoomFactor());
  }

  public static double steppedZoom(double zoom, double steps, int viewWidth, int viewHeight) {
    double baseZoom = focusZoom(0, viewWidth, viewHeight);
    double depthFactor = depthZoomFactor();
    double level = Math.log(zoom / baseZoom) / Math.log(depthFactor) * ZOOM_STEPS_PER_DEPTH;
    double target = level + steps;
    if (steps == Math.rint(steps)) {
      target = Math.round(target);
    }
    return baseZoom * Math.pow(depthFactor, target / ZOOM_STEPS_PER_DEPTH);
  }

  public static double focusZoom(int depth, int viewWidth, int viewHeight) {
    return focusNodePx(viewWidth, viewHeight) / size(depth);
  }

  private static double focusNodePx(int viewWidth, int viewHeight) {
    double childReach = edgeLength(1) + size(1) / 2;
    double fitPx = Math.min(viewWidth, viewHeight) / 2.0 * FOCUS_FILL / childReach * size(0);
    return Math.max(fitPx, MIN_FOCUS_NODE_PX);
  }

  private static double fadeOutPx(double focusNodePx) {
    return focusNodePx * size(2) / size(0);
  }

  private static double fadeLargeEndPx(double focusNodePx) {
    return focusNodePx * size(0) / size(3);
  }

  public static WorldPosition clampFocus(double focusX, double focusY, double zoom, int maxDepth, int viewWidth, int viewHeight,
      double marginPx) {
    double focusNodePx = focusNodePx(viewWidth, viewHeight);
    int firstDepth = 0;
    while (firstDepth < maxDepth && size(firstDepth) * zoom >= fadeLargeEndPx(focusNodePx)) {
      firstDepth++;
    }
    int starDepth = firstDepth;
    while (starDepth < maxDepth && size(starDepth) * zoom > fadeOutPx(focusNodePx)) {
      starDepth++;
    }
    int lastDepth = Math.min(maxDepth, starDepth + 1);
    double halfX = Math.max(0, viewWidth / 2.0 - marginPx) / zoom;
    double halfY = Math.max(0, viewHeight / 2.0 - marginPx) / zoom;
    FocusClamp clamp = new FocusClamp(focusX, focusY, halfX, halfY, firstDepth, lastDepth);
    clamp.search(0, 0, 0);
    return new WorldPosition(clamp.bestX, clamp.bestY);
  }

  private static final class FocusClamp {
    private final double focusX, focusY, halfX, halfY;
    private final int firstDepth, lastDepth;
    private double bestX, bestY;
    private double bestDistSq = Double.POSITIVE_INFINITY;

    private FocusClamp(double focusX, double focusY, double halfX, double halfY, int firstDepth, int lastDepth) {
      this.focusX = focusX;
      this.focusY = focusY;
      this.halfX = halfX;
      this.halfY = halfY;
      this.firstDepth = firstDepth;
      this.lastDepth = lastDepth;
      this.bestX = focusX;
      this.bestY = focusY;
    }

    private void search(double worldX, double worldY, int depth) {
      double reach = ROOT_EDGE_LENGTH * DEPTH_SCALE_POWERS[depth] / (1 - DEPTH_SCALE);
      if (bestDistSq == 0 || boxDistSq(worldX, worldY, halfX + reach, halfY + reach) >= bestDistSq) {
        return;
      }
      if (depth >= firstDepth) {
        double distSq = boxDistSq(worldX, worldY, halfX, halfY);
        if (distSq < bestDistSq) {
          bestDistSq = distSq;
          bestX = Math.clamp(focusX, worldX - halfX, worldX + halfX);
          bestY = Math.clamp(focusY, worldY - halfY, worldY + halfY);
        }
      }
      if (depth >= lastDepth) {
        return;
      }
      double length = edgeLength(depth + 1);
      for (StaffDirection dir : StaffDirection.values()) {
        search(worldX + edgeDirX(dir, depth) * length, worldY + edgeDirY(dir, depth) * length, depth + 1);
      }
    }

    private double boxDistSq(double centerX, double centerY, double extentX, double extentY) {
      double dx = Math.max(0, Math.abs(focusX - centerX) - extentX);
      double dy = Math.max(0, Math.abs(focusY - centerY) - extentY);
      return dx * dx + dy * dy;
    }
  }

  private static double[][] edgeDirections(boolean x) {
    double[][] result = new double[2][StaffDirection.values().length];
    for (int parity = 0; parity < 2; parity++) {
      double angle = ROTATION * (parity == 0 ? 1 : -1);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      for (StaffDirection dir : StaffDirection.values()) {
        Vec2 base = dir.asVec2();
        result[parity][dir.ordinal()] = x ? base.x * cos - base.y * sin : base.x * sin + base.y * cos;
      }
    }
    return result;
  }

  private static double edgeDirX(StaffDirection dir, int parentDepth) {
    return EDGE_DIR_X[parentDepth & 1][dir.ordinal()];
  }

  private static double edgeDirY(StaffDirection dir, int parentDepth) {
    return EDGE_DIR_Y[parentDepth & 1][dir.ordinal()];
  }

  private static double[] depthScalePowers(int maxDepth) {
    double[] powers = new double[maxDepth + 1];
    for (int depth = 0; depth <= maxDepth; depth++) {
      powers[depth] = Math.pow(DEPTH_SCALE, depth);
    }
    return powers;
  }

  private static double size(int depth) {
    return ROOT_SIZE * DEPTH_SCALE_POWERS[depth];
  }

  private static double edgeLength(int depth) {
    return ROOT_EDGE_LENGTH * DEPTH_SCALE_POWERS[depth - 1];
  }

  private static double subtreeRadius(int depth) {
    return ROOT_EDGE_LENGTH * DEPTH_SCALE_POWERS[depth] / (1 - DEPTH_SCALE) + size(depth) / 2;
  }

  private float sizePx(int depth) {
    return (float) (size(depth) * zoom);
  }

  private void drawEdge(int parentDepth, double parentWorldX, double parentWorldY, CrystalBallNode child, int childDepth,
      double childWorldX, double childWorldY, StaffDirection dir) {
    float childPx = sizePx(childDepth);
    float parentPx = sizePx(parentDepth);
    float v = visibility(childPx) * fadeLargeEdges(parentPx);
    if (v <= 0f) {
      return;
    }

    float laneStart = laneOf(parentPx);
    float laneEnd = laneOf(childPx);

    float rx = (float) -edgeDirY(dir, parentDepth);
    float ry = (float) edgeDirX(dir, parentDepth);
    float sideStart = parentDepth % 2 == 0 ? 1 : -1;
    float sideEnd = -sideStart;

    float ax = (float) localX(parentWorldX) + rx * laneStart * sideStart;
    float ay = (float) localY(parentWorldY) + ry * laneStart * sideStart;
    float bx = (float) localX(childWorldX) + rx * laneEnd * sideEnd;
    float by = (float) localY(childWorldY) + ry * laneEnd * sideEnd;

    edges.add(new Edge(ax, ay, bx, by, parentDepth, v));
  }

  private void drawConnection(Edge edge, boolean outline) {
    float ax = edge.ax();
    float ay = edge.ay();
    float bx = edge.bx();
    float by = edge.by();
    float fade = edge.fade();
    float dx = bx - ax;
    float dy = by - ay;
    float len = Mth.sqrt(dx * dx + dy * dy);
    if (len < 1e-4f) {
      return;
    }
    boolean steep = Math.abs(dy) > Math.abs(dx);
    float ua = steep ? ay : ax;
    float va = steep ? ax : ay;
    float ub = steep ? by : bx;
    float vb = steep ? bx : by;
    float slope = (vb - va) / (ub - ua);
    float alongPerU = len / (ub - ua);

    int uMin = steep ? Mth.floor(vy0 - offsetY) : Mth.floor(vx0 - offsetX);
    int uMax = steep ? Mth.ceil(vy1 - offsetY) : Mth.ceil(vx1 - offsetX);
    int us = Math.max(Mth.floor(Math.min(ua, ub)), uMin);
    int ue = Math.min(Mth.ceil(Math.max(ua, ub)), uMax);
    if (ue <= us) {
      return;
    }

    float tail = Math.max(PULSE_MIN_TAIL, len * PULSE_TAIL);
    float firstHead =
        (float) (Mth.positiveModulo(time - edge.parentDepth() * PULSE_EDGE_DURATION, PULSE_INTERVAL) / PULSE_EDGE_DURATION * len);
    float headSpacing = (float) (PULSE_INTERVAL / PULSE_EDGE_DURATION * len);
    int alpha = alpha255(fade);
    int outlineColor = ARGB.color(alpha, OUTLINE_COLOR);

    int runStart = us;
    int runV = 0;
    int runColor = 0;
    for (int u = us; u <= ue; u++) {
      int v = 0;
      int color = 0;
      if (u < ue) {
        v = connectionV(va, slope, ua, u);
        if (outline) {
          color = outlineColor;
        } else {
          float along = (u + 0.5f - ua) * alongPerU;
          float behind = behindPulse(along, firstHead, headSpacing);
          color = ARGB.color(alpha, pulseColor(behind, tail));
          if (behind < tail * PULSE_HALO) {
            int halo = ARGB.color(alpha255(fade * pulseAlpha(behind, tail) * PULSE_HALO_ALPHA), pulseColor);
            emit(steep, u, v - 1, u + 1, v, halo);
            emit(steep, u, v + CONNECTION_WIDTH, u + 1, v + CONNECTION_WIDTH + 1, halo);
          }
        }
      }
      if (u == us) {
        runV = v;
        runColor = color;
      } else if (u == ue || v != runV || color != runColor) {
        if (outline) {
          emit(steep, runStart, runV - 1, u, runV, runColor);
          emit(steep, runStart, runV + CONNECTION_WIDTH, u, runV + CONNECTION_WIDTH + 1, runColor);
        } else {
          emit(steep, runStart, runV, u, runV + CONNECTION_WIDTH, runColor);
        }
        runStart = u;
        runV = v;
        runColor = color;
      }
    }
    if (outline) {
      int startV = connectionV(va, slope, ua, us);
      int endV = connectionV(va, slope, ua, ue - 1);
      emit(steep, us - 1, startV, us, startV + CONNECTION_WIDTH, outlineColor);
      emit(steep, ue, endV, ue + 1, endV + CONNECTION_WIDTH, outlineColor);
    }
  }

  private static float behindPulse(float along, float firstHead, float headSpacing) {
    float index = Math.max(0, Mth.ceil((along - 1 - firstHead) / headSpacing));
    return firstHead + index * headSpacing - along;
  }

  private static float pulseAlpha(float behind, float tail) {
    if (behind < 0) {
      return Math.max(0, 1 + behind);
    }
    return behind < 1 ? 1 : Math.max(0, 1 - behind / tail);
  }

  private int pulseColor(float behind, float tail) {
    float a = pulseAlpha(behind, tail);
    if (a <= 0) {
      return connectionColor;
    }
    return ARGB.srgbLerp(a, connectionColor, behind < 1 ? pulseHeadColor : pulseColor);
  }

  private static int connectionV(float va, float slope, float ua, int u) {
    float v = va + slope * (u + 0.5f - ua) - CONNECTION_WIDTH / 2f;
    return Math.round(Math.clamp(v, -COORD_LIMIT, COORD_LIMIT));
  }

  private void emit(boolean steep, int u0, int v0, int u1, int v1, int color) {
    int x0 = steep ? v0 : u0;
    int y0 = steep ? u0 : v0;
    int x1 = steep ? v1 : u1;
    int y1 = steep ? u1 : v1;
    if (x1 > x0 && y1 > y0) {
      graphics.fill(x0, y0, x1, y1, color);
    }
  }

  private static int brighter(int rgb, float saturationFactor) {
    float r = ARGB.red(rgb) / 255f;
    float g = ARGB.green(rgb) / 255f;
    float b = ARGB.blue(rgb) / 255f;
    float max = Math.max(r, Math.max(g, b));
    float delta = max - Math.min(r, Math.min(g, b));
    float hue = 0;
    if (delta > 0) {
      if (max == r) {
        hue = (g - b) / delta;
      } else if (max == g) {
        hue = 2 + (b - r) / delta;
      } else {
        hue = 4 + (r - g) / delta;
      }
      hue = Mth.positiveModulo(hue / 6, 1f);
    }
    float saturation = max > 0 ? delta / max : 0;
    return Mth.hsvToRgb(hue, saturation * saturationFactor, 1);
  }

  private static float laneOf(float nodePx) {
    int nodeCore = Math.max(1, Math.round(nodePx / ROOT_SIZE));
    float lane = Math.max(2, LANE_FACTOR * nodeCore);
    return Math.min(lane, nodePx * 0.3f);
  }

  private void drawNode(Font font, Visible v) {
    float sizePx = sizePx(v.depth());
    float fade = visibility(sizePx);
    if (fade <= 0f) {
      return;
    }
    int size = Math.round(sizePx);
    if (size < 2) {
      return;
    }
    float lx = (float) localX(v.worldX());
    float ly = (float) localY(v.worldY());

    CrystalBallNodeState state = states.stateOf(v.node());
    Identifier sprite = frameSprite(state);
    int alpha = alpha255(fade);
    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(lx, ly);
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, -size / 2, -size / 2, size, size, ARGB.color(alpha, 0xFFFFFF));
    pose.popMatrix();

    if (state == CrystalBallNodeState.UNKNOWN && sizePx >= 10 && fade >= 0.25f) {
      drawQuestionMark(font, lx, ly, sizePx, alpha);
    } else if ((state == CrystalBallNodeState.SPELL || state == CrystalBallNodeState.ROOT) && sizePx >= 10 && fade >= 0.25f) {
      states.iconOf(v.node()).ifPresent(icon -> drawIcon(icon, lx, ly, sizePx, alpha));
    }
  }

  static Identifier frameSprite(CrystalBallNodeState state) {
    return switch (state) {
      case ROOT -> FRAME_ROOT;
      case UNKNOWN -> FRAME_UNKNOWN;
      case EXPLORED -> FRAME_EXPLORED;
      case SPELL -> FRAME_SPELL;
    };
  }

  private void drawQuestionMark(Font font, float lx, float ly, float sizePx, int alpha) {
    float scale = (sizePx / ROOT_SIZE) * 1.5f;
    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(lx, ly);
    pose.scale(scale, scale);
    graphics.centeredText(font, "?", 0, -font.lineHeight / 2, ARGB.color(alpha, QUESTION_MARK));
    pose.popMatrix();
  }

  private void drawIcon(Identifier icon, float lx, float ly, float sizePx, int alpha) {
    int size = Math.round(sizePx * ICON_SIZE_FACTOR);
    if (size < 1) {
      return;
    }
    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(lx, ly);
    graphics.blit(RenderPipelines.GUI_TEXTURED, icon, -size / 2, -size / 2, 0, 0, size, size, size, size, ARGB.color(alpha, 0xFFFFFF));
    pose.popMatrix();
  }

  private void drawStars(boolean outline) {
    for (Visible v : bigStars) {
      drawStar(v, BIG_STAR_MIN_SIZE, BIG_STAR_MAX_SIZE, true, outline);
    }
    for (Visible v : stars) {
      drawStar(v, STAR_MIN_SIZE, STAR_MAX_SIZE, false, outline);
    }
  }

  private void drawStar(Visible v, int minSize, int maxSize, boolean sparkle, boolean outline) {
    double sx = screenX(v.worldX());
    double sy = screenY(v.worldY());
    if (sx < vx0 - maxSize || sx > vx1 + maxSize || sy < vy0 - maxSize || sy > vy1 + maxSize) {
      return;
    }

    int hash = Mth.murmurHash3Mixer(v.node().path().hashCode());
    int size = minSize + Math.round(unit(hash) * (maxSize - minSize));

    float speed = 1.2f + unit(hash >>> 8) * 1.8f;
    float phase = unit(hash >>> 16) * (float) (Math.PI * 2);
    float brightness = 0.5f + 0.5f * (float) Math.sin(time * speed + phase);
    int alpha = alpha255(brightness);
    int color = ARGB.color(alpha, 0xFFFFFF);
    int arm = size / 2;
    boolean diagonals = sparkle && arm >= 2 && unit(hash >>> 24) < SPARKLE_CHANCE;

    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate((float) localX(v.worldX()), (float) localY(v.worldY()));
    if (outline) {
      int outlineColor = ARGB.color(alpha, OUTLINE_COLOR);
      int[] runs = STAR_OUTLINES[diagonals ? 1 : 0][size];
      for (int i = 0; i < runs.length; i += 3) {
        graphics.fill(runs[i], runs[i + 1], runs[i] + runs[i + 2], runs[i + 1] + 1, outlineColor);
      }
    } else if (size <= 2) {
      graphics.fill(-size / 2, -size / 2, size - size / 2, size - size / 2, color);
    } else {
      graphics.fill(-arm, 0, arm + 1, 1, color);
      graphics.fill(0, -arm, 1, arm + 1, color);
      if (diagonals) {
        drawDiagonals(arm / 2, ARGB.color(alpha / 2, 0xFFFFFF));
      }
    }
    pose.popMatrix();
  }

  private static int[][][] starOutlines() {
    int[][][] outlines = new int[2][BIG_STAR_MAX_SIZE + 1][];
    for (int size = STAR_MIN_SIZE; size <= BIG_STAR_MAX_SIZE; size++) {
      outlines[0][size] = starOutline(size, false);
      outlines[1][size] = starOutline(size, true);
    }
    return outlines;
  }

  private static int[] starOutline(int size, boolean diagonals) {
    int radius = size / 2 + 2;
    int side = 2 * radius + 1;
    boolean[][] filled = new boolean[side][side];
    if (size <= 2) {
      for (int y = -size / 2; y < size - size / 2; y++) {
        for (int x = -size / 2; x < size - size / 2; x++) {
          filled[radius + y][radius + x] = true;
        }
      }
    } else {
      int arm = size / 2;
      for (int i = -arm; i <= arm; i++) {
        filled[radius][radius + i] = true;
        filled[radius + i][radius] = true;
      }
      if (diagonals) {
        for (int i = 1; i <= arm / 2; i++) {
          filled[radius + i][radius + i] = true;
          filled[radius + i][radius - i] = true;
          filled[radius - i][radius + i] = true;
          filled[radius - i][radius - i] = true;
        }
      }
    }

    IntList runs = new IntArrayList();
    for (int y = 0; y < side; y++) {
      int runStart = -1;
      for (int x = 0; x <= side; x++) {
        boolean outlined = x < side && isStarOutline(filled, x, y);
        if (outlined && runStart < 0) {
          runStart = x;
        } else if (!outlined && runStart >= 0) {
          runs.add(runStart - radius);
          runs.add(y - radius);
          runs.add(x - runStart);
          runStart = -1;
        }
      }
    }
    return runs.toIntArray();
  }

  private static boolean isStarOutline(boolean[][] filled, int x, int y) {
    int last = filled.length - 1;
    if (filled[y][x]) {
      return false;
    }
    return x > 0 && filled[y][x - 1] || x < last && filled[y][x + 1] || y > 0 && filled[y - 1][x] || y < last && filled[y + 1][x];
  }

  private void drawDiagonals(int arm, int color) {
    for (int i = 1; i <= arm; i++) {
      graphics.fill(i, i, i + 1, i + 1, color);
      graphics.fill(-i, i, -i + 1, i + 1, color);
      graphics.fill(i, -i, i + 1, -i + 1, color);
      graphics.fill(-i, -i, -i + 1, -i + 1, color);
    }
  }

  private static float unit(int bits) {
    return (bits & 0xFF) / 255f;
  }

  private float fadeSmall(float sizePx) {
    return Math.clamp((sizePx - fadeOutPx) / (fadeInPx - fadeOutPx), 0, 1);
  }

  private float fadeLarge(float sizePx) {
    return Math.clamp(1 - (sizePx - fadeLargeMidPx) / (fadeLargeEndPx - fadeLargeMidPx), 0, 1);
  }

  private float fadeLargeEdges(float parentPx) {
    return Math.clamp(1 - (parentPx - fadeLargeStartPx) / (fadeLargeMidPx - fadeLargeStartPx), 0, 1);
  }

  private float visibility(float sizePx) {
    return fadeSmall(sizePx) * fadeLarge(sizePx);
  }

  private static int alpha255(float fade) {
    return Math.clamp(Math.round(fade * 255), 0, 255);
  }

  private double localX(double worldX) {
    return centerX + (worldX - anchorX) * zoom;
  }

  private double localY(double worldY) {
    return centerY + (worldY - anchorY) * zoom;
  }

  private double screenX(double worldX) {
    return localX(worldX) + offsetX;
  }

  private double screenY(double worldY) {
    return localY(worldY) + offsetY;
  }
}
