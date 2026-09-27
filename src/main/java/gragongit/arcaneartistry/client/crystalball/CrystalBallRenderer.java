package gragongit.arcaneartistry.client.crystalball;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class CrystalBallRenderer {

  @FunctionalInterface
  public interface ChrystalBallNodeStateProvider {
    CrystalBallNodeState stateOf(CrystalBallNode node);

    default Optional<Identifier> iconOf(CrystalBallNode node) {
      return Optional.empty();
    }
  }

  public record WorldPosition(double x, double y) {
  }

  private record Visible(CrystalBallNode node, double worldX, double worldY, int depth) {
  }

  private static final int LINE_OUTLINE = 0x000000;
  private static final int LINE_KNOWN = 0xFFFFFF;
  private static final int LINE_UNKNOWN = 0x808080;
  private static final int QUESTION_MARK = 0xA0A0A0;

  private static final int STAR_MIN_SIZE = 1;
  private static final int STAR_MAX_SIZE = 3;
  private static final int BIG_STAR_MIN_SIZE = 5;
  private static final int BIG_STAR_MAX_SIZE = 9;
  private static final float SPARKLE_CHANCE = 0.5f;
  private static final float STAR_HIT_PADDING = 2;

  private static final Identifier FRAME_ROOT = Identifier.withDefaultNamespace("advancements/goal_frame_obtained");
  private static final Identifier FRAME_UNKNOWN = Identifier.withDefaultNamespace("advancements/task_frame_unobtained");
  private static final Identifier FRAME_EXPLORED = Identifier.withDefaultNamespace("advancements/task_frame_obtained");
  private static final Identifier FRAME_SPELL = Identifier.withDefaultNamespace("advancements/challenge_frame_obtained");
  private static final float ICON_SIZE_FACTOR = 0.6f;

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

  private static final float COORD_LIMIT = 1_000_000f;

  private final int maxDepth;
  private final List<Visible> visible = new ArrayList<>();
  private final List<Visible> bigStars = new ArrayList<>();
  private final List<Visible> stars = new ArrayList<>();

  private double anchorX;
  private double anchorY;
  private double anchorZoom = Double.NaN;

  private GuiGraphicsExtractor graphics;
  private ChrystalBallNodeStateProvider states;
  private int vx0, vy0, vx1, vy1;
  private double zoom;
  private float centerX, centerY;
  private double offsetX, offsetY;
  private float time;
  private float fadeInPx, fadeOutPx, fadeLargeStartPx, fadeLargeEndPx;

  public CrystalBallRenderer(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public void render(GuiGraphicsExtractor graphics, Font font, CrystalBallNode root, CrystalBallCamera camera,
      ChrystalBallNodeStateProvider states, int x0, int y0, int x1, int y1) {
    this.graphics = graphics;
    this.states = states;
    this.vx0 = x0;
    this.vy0 = y0;
    this.vx1 = x1;
    this.vy1 = y1;
    this.centerX = (x0 + x1) / 2f;
    this.centerY = (y0 + y1) / 2f;
    double focusNodePx = focusNodePx(x1 - x0, y1 - y0);
    this.fadeInPx = (float) (focusNodePx * size(1) / size(0));
    this.fadeOutPx = (float) (focusNodePx * size(2) / size(0));
    this.fadeLargeStartPx = (float) (focusNodePx * size(0) / size(2));
    this.fadeLargeEndPx = (float) (focusNodePx * size(0) / size(3));
    this.zoom = camera.zoom();
    if (zoom != anchorZoom) {
      anchorZoom = zoom;
      anchorX = camera.focusX();
      anchorY = camera.focusY();
    }
    this.offsetX = camera.panX() + anchorX * zoom;
    this.offsetY = camera.panY() + anchorY * zoom;
    this.time = System.nanoTime() / 1_000_000_000f;

    visible.clear();
    bigStars.clear();
    stars.clear();
    graphics.enableScissor(x0, y0, x1, y1);

    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate((float) offsetX, (float) offsetY);
    collect(root, 0, 0, 0);
    for (Visible v : visible) {
      drawNode(font, v);
    }
    for (Visible v : bigStars) {
      drawStar(v, BIG_STAR_MIN_SIZE, BIG_STAR_MAX_SIZE, true);
    }
    for (Visible v : stars) {
      drawStar(v, STAR_MIN_SIZE, STAR_MAX_SIZE, false);
    }
    pose.popMatrix();
    graphics.disableScissor();

    this.graphics = null;
    this.states = null;
  }

  public Optional<CrystalBallNode> nodeAt(double mouseX, double mouseY) {
    if (mouseX < vx0 || mouseX > vx1 || mouseY < vy0 || mouseY > vy1) {
      return Optional.empty();
    }
    for (int i = visible.size() - 1; i >= 0; i--) {
      Visible v = visible.get(i);
      float sizePx = sizePx(v.depth());
      if (visibility(sizePx) <= 0f || Math.round(sizePx) < 2) {
        continue;
      }
      float half = sizePx / 2;
      if (Math.abs(mouseX - screenX(v.worldX())) <= half && Math.abs(mouseY - screenY(v.worldY())) <= half) {
        return Optional.of(v.node());
      }
    }
    Visible nearest = nearestStar(bigStars, mouseX, mouseY, BIG_STAR_MAX_SIZE / 2f + STAR_HIT_PADDING, null);
    nearest = nearestStar(stars, mouseX, mouseY, STAR_MAX_SIZE / 2f + STAR_HIT_PADDING, nearest);
    return Optional.ofNullable(nearest).map(Visible::node);
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

  /** Distance from the root to the outermost nodes, which are the ends of the straight paths like RRRR... */
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

  /** Allows zooming one depth further than focusing a node of the deepest depth. */
  public static double maxZoom(int maxDepth, int viewWidth, int viewHeight) {
    return focusZoom(maxDepth, viewWidth, viewHeight) * size(0) / size(1);
  }

  public static double steppedZoom(double zoom, double steps, int viewWidth, int viewHeight) {
    double baseZoom = focusZoom(0, viewWidth, viewHeight);
    double depthFactor = size(0) / size(1);
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
    float v = visibility(childPx) * fadeLarge(parentPx);
    if (v <= 0f) {
      return;
    }
    int alpha = alpha255(v);

    float k = childPx / ROOT_SIZE;
    int core = Math.max(1, Math.round(k));
    int outline = core * 3;

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

    int coreColor = states.stateOf(child) == CrystalBallNodeState.UNKNOWN ? LINE_UNKNOWN : LINE_KNOWN;
    drawLine(ax, ay, bx, by, outline, ARGB.color(alpha, LINE_OUTLINE));
    drawLine(ax, ay, bx, by, core, ARGB.color(alpha, coreColor));
  }

  private static float laneOf(float nodePx) {
    int nodeCore = Math.max(1, Math.round(nodePx / ROOT_SIZE));
    float lane = Math.max(2, LANE_FACTOR * nodeCore);
    return Math.min(lane, nodePx * 0.3f);
  }

  private void drawLine(float ax, float ay, float bx, float by, int width, int color) {
    float dx = bx - ax;
    float dy = by - ay;
    float len = Mth.sqrt(dx * dx + dy * dy);
    if (len < 1e-4f) {
      return;
    }
    float half = width / 2f;
    float ex = dx / len * half;
    float ey = dy / len * half;
    ax -= ex;
    ay -= ey;
    bx += ex;
    by += ey;

    boolean steep = Math.abs(dy) > Math.abs(dx);
    float u0 = steep ? ay : ax;
    float v0 = steep ? ax : ay;
    float u1 = steep ? by : bx;
    float v1 = steep ? bx : by;
    if (u0 > u1) {
      float t = u0;
      u0 = u1;
      u1 = t;
      t = v0;
      v0 = v1;
      v1 = t;
    }
    float slope = (v1 - v0) / (u1 - u0);

    int uMin = steep ? Mth.floor(vy0 - offsetY) : Mth.floor(vx0 - offsetX);
    int uMax = steep ? Mth.ceil(vy1 - offsetY) : Mth.ceil(vx1 - offsetX);
    int us = Math.max(Mth.floor(u0), uMin);
    int ue = Math.min(Mth.ceil(u1), uMax);
    if (ue <= us) {
      return;
    }

    int runStart = us;
    int runV = lineV(v0, slope, u0, us, half);
    for (int u = us + 1; u < ue; u++) {
      int v = lineV(v0, slope, u0, u, half);
      if (v != runV) {
        emit(steep, runStart, runV, u, runV + width, color);
        runStart = u;
        runV = v;
      }
    }
    emit(steep, runStart, runV, ue, runV + width, color);
  }

  private static int lineV(float v0, float slope, float u0, int u, float half) {
    float v = v0 + slope * (u + 0.5f - u0) - half;
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
    Identifier sprite = switch (state) {
      case ROOT -> FRAME_ROOT;
      case UNKNOWN -> FRAME_UNKNOWN;
      case EXPLORED -> FRAME_EXPLORED;
      case SPELL -> FRAME_SPELL;
    };
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

  private void drawStar(Visible v, int minSize, int maxSize, boolean sparkle) {
    double sx = screenX(v.worldX());
    double sy = screenY(v.worldY());
    if (sx < vx0 - maxSize || sx > vx1 + maxSize || sy < vy0 - maxSize || sy > vy1 + maxSize) {
      return;
    }

    int hash = Mth.murmurHash3Mixer(v.node().path().hashCode());
    int size = minSize + Math.round(unit(hash) * (maxSize - minSize));

    float speed = 1.2f + unit(hash >>> 8) * 1.8f;
    float phase = unit(hash >>> 16) * (float) (Math.PI * 2);
    float brightness = 0.5f + 0.5f * Mth.sin(time * speed + phase);
    int alpha = alpha255(brightness);
    int color = ARGB.color(alpha, 0xFFFFFF);

    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate((float) localX(v.worldX()), (float) localY(v.worldY()));
    if (size <= 2) {
      graphics.fill(-size / 2, -size / 2, size - size / 2, size - size / 2, color);
    } else {
      int arm = size / 2;
      graphics.fill(-arm, 0, arm + 1, 1, color);
      graphics.fill(0, -arm, 1, arm + 1, color);
      if (sparkle && arm >= 2 && unit(hash >>> 24) < SPARKLE_CHANCE) {
        drawDiagonals(arm / 2, ARGB.color(alpha / 2, 0xFFFFFF));
      }
    }
    pose.popMatrix();
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
    return Math.clamp(1 - (sizePx - fadeLargeStartPx) / (fadeLargeEndPx - fadeLargeStartPx), 0, 1);
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
