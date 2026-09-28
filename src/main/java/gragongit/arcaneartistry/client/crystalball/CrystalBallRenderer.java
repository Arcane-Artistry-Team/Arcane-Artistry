package gragongit.arcaneartistry.client.crystalball;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.staff.MeteorColors;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class CrystalBallRenderer {

  public interface CrystalBallNodeStateProvider {
    CrystalBallNodeState stateOf(CrystalBallNode node);

    MeteorColors meteorColors();

    default Optional<Identifier> iconOf(CrystalBallNode node) {
      return Optional.empty();
    }
  }

  public record WorldPosition(double x, double y) {
  }

  private record Visible(CrystalBallNode node, double worldX, double worldY, int depth) {
  }

  private static final int QUESTION_MARK = 0xA0A0A0;

  private static final int METEORS_PER_EDGE = 6;
  private static final float METEOR_SPACING_JITTER = 0.3f;
  private static final double METEOR_FLOW_SPEED = 0.1;
  private static final float METEOR_SIZE = 0.12f;
  private static final float METEOR_SIZE_JITTER = 0.25f;
  private static final float WOBBLE_AMPLITUDE = 0.07f;
  private static final float WOBBLE_MIN_SPEED = 1.5f;
  private static final float WOBBLE_MAX_SPEED = 2.8f;

  private static final float HEAD_SHADE_CURVE = 1.4f;
  private static final List<Integer> HEAD_UNKNOWN = List.of(0xF0F0F0, 0xC8C8C8, 0x9A9A9A);

  private static final float FLAME_LENGTH = 6;
  private static final float FLAME_EDGE_JITTER = 0.35f;
  private static final float FLAME_NOISE_SCALE = 0.45f;
  private static final float FLAME_TURBULENCE = 0.5f;
  private static final double FLAME_SCROLL_SPEED = 7;
  private static final float FLAME_FLICKER = 0.08f;
  private static final float FLAME_HEAT_MAX = 1.1f;
  private static final float FLAME_HEAT_MIN = 0.1f;
  private static final float[] FLAME_TIP_ALPHA = {0.6f, 0.9f};
  private static final List<Integer> FLAME_UNKNOWN = List.of(0xE8E8E8, 0xBDBDBD, 0x969696, 0x707070, 0x4A4A4A);

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

  private final int maxDepth;
  private final List<Visible> visible = new ArrayList<>();
  private final List<Visible> bigStars = new ArrayList<>();
  private final List<Visible> stars = new ArrayList<>();

  private double anchorX;
  private double anchorY;
  private double anchorZoom = Double.NaN;

  private GuiGraphicsExtractor graphics;
  private CrystalBallNodeStateProvider states;
  private int vx0, vy0, vx1, vy1;
  private double zoom;
  private float centerX, centerY;
  private double offsetX, offsetY;
  private double time;
  private float fadeInPx, fadeOutPx, fadeLargeStartPx, fadeLargeEndPx;

  public CrystalBallRenderer(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public void render(GuiGraphicsExtractor graphics, Font font, CrystalBallNode root, CrystalBallCamera camera,
      CrystalBallNodeStateProvider states, int x0, int y0, int x1, int y1) {
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
    this.fadeOutPx = (float) fadeOutPx(focusNodePx);
    this.fadeLargeStartPx = (float) (focusNodePx * size(0) / size(2));
    this.fadeLargeEndPx = (float) fadeLargeEndPx(focusNodePx);
    this.zoom = camera.zoom();
    if (zoom != anchorZoom) {
      anchorZoom = zoom;
      anchorX = camera.focusX();
      anchorY = camera.focusY();
    }
    this.offsetX = camera.panX() + anchorX * zoom;
    this.offsetY = camera.panY() + anchorY * zoom;
    this.time = System.nanoTime() / 1_000_000_000.0;

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
    float v = visibility(childPx) * fadeLarge(parentPx);
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

    float dx = bx - ax;
    float dy = by - ay;
    float len = Mth.sqrt(dx * dx + dy * dy);
    if (len < 1e-4f) {
      return;
    }
    float ux = dx / len;
    float uy = dy / len;

    boolean unknown = states.stateOf(child) == CrystalBallNodeState.UNKNOWN;
    float amplitude = childPx * WOBBLE_AMPLITUDE;
    float baseRadius = childPx * METEOR_SIZE / 2;
    int edgeHash = Mth.murmurHash3Mixer(child.path().hashCode());

    for (int i = 0; i < METEORS_PER_EDGE; i++) {
      int hash = Mth.murmurHash3Mixer(edgeHash + i);
      double slot = (i + (unit(hash) - 0.5f) * METEOR_SPACING_JITTER) / METEORS_PER_EDGE;
      float radius = Math.max(1, baseRadius * (1 + (unit(hash >>> 8) - 0.5f) * 2 * METEOR_SIZE_JITTER));
      float wobbleSpeed = Mth.lerp(unit(hash >>> 16), WOBBLE_MIN_SPEED, WOBBLE_MAX_SPEED);
      float wobblePhase = unit(hash >>> 24) * Mth.TWO_PI;
      float s = (float) frac(time * METEOR_FLOW_SPEED + slot);

      float envelope = Mth.sin(Mth.PI * s);
      double wobbleAngle = time * wobbleSpeed + wobblePhase;
      float wobble = amplitude * envelope * (float) Math.sin(wobbleAngle);
      float along = s * len;
      float x = ax + ux * along - uy * wobble;
      float y = ay + uy * along + ux * wobble;

      float wobbleSlope = amplitude * (Mth.PI * Mth.cos(Mth.PI * s) * (float) Math.sin(wobbleAngle)
          + envelope * (float) Math.cos(wobbleAngle) * wobbleSpeed / (float) METEOR_FLOW_SPEED);
      float tx = ux * len - uy * wobbleSlope;
      float ty = uy * len + ux * wobbleSlope;
      float tl = Mth.sqrt(tx * tx + ty * ty);

      drawMeteor(x, y, tx / tl, ty / tl, radius, hash, v, unknown);
    }
  }

  private void drawMeteor(float x, float y, float dirX, float dirY, float radius, int seed, float fade, boolean unknown) {
    if (fade <= 0f) {
      return;
    }
    int hx = Mth.floor(x);
    int hy = Mth.floor(y);
    float length = radius * FLAME_LENGTH;
    float pad = radius + 2;

    float tailX = hx - dirX * length;
    float tailY = hy - dirY * length;
    int minX = Math.max(Mth.floor(Math.min(hx, tailX) - pad), Mth.floor(vx0 - offsetX));
    int maxX = Math.min(Mth.ceil(Math.max(hx, tailX) + pad), Mth.ceil(vx1 - offsetX));
    int minY = Math.max(Mth.floor(Math.min(hy, tailY) - pad), Mth.floor(vy0 - offsetY));
    int maxY = Math.min(Mth.ceil(Math.max(hy, tailY) + pad), Mth.ceil(vy1 - offsetY));
    if (minX > maxX || minY > maxY) {
      return;
    }

    MeteorColors colors = states.meteorColors();
    List<Integer> head = unknown ? HEAD_UNKNOWN : colors.head();
    List<Integer> flame = unknown ? FLAME_UNKNOWN : colors.flame();
    float scroll = (float) ((time * FLAME_SCROLL_SPEED) % 256);
    float flicker = FLAME_FLICKER * (float) Math.sin(time * 17 + unit(seed >>> 4) * Mth.TWO_PI);
    float radiusSq = radius * radius;

    for (int py = minY; py <= maxY; py++) {
      int runStart = minX;
      int runColor = 0;
      for (int px = minX; px <= maxX + 1; px++) {
        int color = 0;
        if (px <= maxX) {
          float qx = px - hx;
          float qy = py - hy;
          float distSq = qx * qx + qy * qy;
          if (distSq <= radiusSq) {
            color = ARGB.color(alpha255(fade), head.get(shade(Mth.sqrt(distSq) / radius, head.size())));
          } else {
            color = flameColor(-(qx * dirX + qy * dirY), qy * dirX - qx * dirY, radius, length, scroll, flicker, seed, flame, fade);
          }
        }
        if (color != runColor) {
          if (runColor != 0) {
            graphics.fill(runStart, py, px, py + 1, runColor);
          }
          runStart = px;
          runColor = color;
        }
      }
    }
  }

  private static int shade(float dist, int count) {
    return Math.min(count - 1, (int) (Math.pow(dist, HEAD_SHADE_CURVE) * count));
  }

  private static int flameColor(float along, float lateral, float radius, float length, float scroll, float flicker, int seed,
      List<Integer> palette, float fade) {
    if (along < 0 || along > length) {
      return 0;
    }
    float t = along / length;
    float flow = along * FLAME_NOISE_SCALE - scroll;
    float sideNoise = valueNoise(flow, lateral < 0 ? 0 : 7, seed);
    float halfWidth = (radius + 0.5f) * (1 - t) * (1 - FLAME_EDGE_JITTER * sideNoise);
    float distance = Math.abs(lateral);
    if (distance > halfWidth) {
      return 0;
    }
    float edge = 1 - distance / halfWidth;
    float noise = valueNoise(flow, lateral * FLAME_NOISE_SCALE * 1.5f + 13, seed);
    float heat = (1 - t) * 0.9f + edge * 0.5f - 0.35f + (noise - 0.5f) * FLAME_TURBULENCE + flicker;
    int count = palette.size();
    int i = Math.clamp(Mth.floor((FLAME_HEAT_MAX - heat) / (FLAME_HEAT_MAX - FLAME_HEAT_MIN) * count), 0, count - 1);
    int fromTip = count - 1 - i;
    float alpha = i > 0 && fromTip < FLAME_TIP_ALPHA.length ? FLAME_TIP_ALPHA[fromTip] : 1;
    return ARGB.color(alpha255(fade * alpha), palette.get(i));
  }

  private static float valueNoise(float x, float y, int seed) {
    int x0 = Mth.floor(x);
    int y0 = Mth.floor(y);
    float fx = x - x0;
    float fy = y - y0;
    fx = fx * fx * (3 - 2 * fx);
    fy = fy * fy * (3 - 2 * fy);
    float top = Mth.lerp(fx, lattice(x0, y0, seed), lattice(x0 + 1, y0, seed));
    float bottom = Mth.lerp(fx, lattice(x0, y0 + 1, seed), lattice(x0 + 1, y0 + 1, seed));
    return Mth.lerp(fy, top, bottom);
  }

  private static float lattice(int x, int y, int seed) {
    return unit(Mth.murmurHash3Mixer(seed ^ (x & 255) * 0x27D4EB2D ^ (y & 255) * 0x165667B1));
  }

  private static float laneOf(float nodePx) {
    int nodeCore = Math.max(1, Math.round(nodePx / ROOT_SIZE));
    float lane = Math.max(2, LANE_FACTOR * nodeCore);
    return Math.min(lane, nodePx * 0.3f);
  }

  private static double frac(double value) {
    return value - Math.floor(value);
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
    float brightness = 0.5f + 0.5f * (float) Math.sin(time * speed + phase);
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
