package gragongit.arcaneartistry.client.crystalball;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  private static final int MAX_DEPTH = 8;
  private static final int STAR_MIN_SIZE = 1;
  private static final int STAR_MAX_SIZE = 3;
  private static final int BIG_STAR_MIN_SIZE = 5;
  private static final int BIG_STAR_MAX_SIZE = 9;
  private static final float SPARKLE_CHANCE = 0.5f;
  private static final float ROOT_SIZE = 26;
  private static final float ROOT_EDGE_LENGTH = ROOT_SIZE * 8;
  private static final float ROTATION_DEGREES = 2.5f;

  private static final Identifier FRAME_ROOT = Identifier.withDefaultNamespace("advancements/goal_frame_obtained");
  private static final Identifier FRAME_UNKNOWN = Identifier.withDefaultNamespace("advancements/task_frame_unobtained");
  private static final Identifier FRAME_EXPLORED = Identifier.withDefaultNamespace("advancements/task_frame_obtained");
  private static final Identifier FRAME_SPELL = Identifier.withDefaultNamespace("advancements/challenge_frame_obtained");

  private static final float FADE_OUT_PX = 10;
  private static final float FADE_IN_PX = 26;
  private static final float FADE_LARGE_START_PX = 120;
  private static final float FADE_LARGE_END_PX = 240;

  private static final int LINE_OUTLINE = 0x000000;
  private static final int LINE_KNOWN = 0xFFFFFF;
  private static final int LINE_UNKNOWN = 0x808080;
  private static final int QUESTION_MARK = 0xA0A0A0;

  private static final float LANE_FACTOR = 5;

  private static final float ICON_SIZE_FACTOR = 0.6f;

  private static final float COORD_LIMIT = 1_000_000f;

  private record Visible(CrystalBallNode node, float worldX, float worldY, int depth) {
  }

  private final List<Visible> visible = new ArrayList<>();
  private final List<Visible> bigStars = new ArrayList<>();
  private final List<Visible> stars = new ArrayList<>();

  private GuiGraphicsExtractor graphics;
  private ChrystalBallNodeStateProvider states;
  private int vx0, vy0, vx1, vy1;
  private float originX, originY, zoom;
  private float centerX, centerY;
  private float panX, panY;
  private float time;

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
    this.panX = camera.panX();
    this.panY = camera.panY();
    this.originX = centerX + panX;
    this.originY = centerY + panY;
    this.zoom = camera.zoom();
    this.time = System.nanoTime() / 1_000_000_000f;

    visible.clear();
    bigStars.clear();
    stars.clear();
    graphics.enableScissor(x0, y0, x1, y1);

    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(panX, panY);
    collect(root, 0, 0f, 0f);
    pose.popMatrix();

    for (Visible v : visible) {
      drawNode(font, v);
    }
    for (Visible v : bigStars) {
      drawStar(v.worldX(), v.worldY(), BIG_STAR_MIN_SIZE, BIG_STAR_MAX_SIZE, true);
    }
    for (Visible v : stars) {
      drawStar(v.worldX(), v.worldY(), STAR_MIN_SIZE, STAR_MAX_SIZE, false);
    }
    graphics.disableScissor();

    visible.clear();
    bigStars.clear();
    stars.clear();
    this.graphics = null;
    this.states = null;
  }

  private void collect(CrystalBallNode node, int depth, float worldX, float worldY) {
    float sizePx = size(depth) * zoom;
    float sx = screenX(worldX);
    float sy = screenY(worldY);

    float r = subtreeRadius(depth) * zoom;
    if (sx + r < vx0 || sx - r > vx1 || sy + r < vy0 || sy - r > vy1) {
      return;
    }

    if (fadeSmall(sizePx) <= 0f) {
      bigStars.add(new Visible(node, worldX, worldY, depth));
      if (depth < MAX_DEPTH) {
        collectSmallStars(node, depth, worldX, worldY);
      }
      return;
    }

    float half = sizePx / 2;
    if (sx + half >= vx0 && sx - half <= vx1 && sy + half >= vy0 && sy - half <= vy1) {
      visible.add(new Visible(node, worldX, worldY, depth));
    }

    if (depth >= MAX_DEPTH) {
      return;
    }
    for (StaffDirection dir : StaffDirection.values()) {
      int childDepth = depth + 1;
      Vec2 edgeDir = rotatedDirection(dir, depth);
      float length = edgeLength(childDepth);
      float childWorldX = worldX + edgeDir.x * length;
      float childWorldY = worldY + edgeDir.y * length;
      CrystalBallNode child = node.child(dir);
      drawEdge(depth, worldX, worldY, child, childDepth, childWorldX, childWorldY, edgeDir);
      collect(child, childDepth, childWorldX, childWorldY);
    }
  }

  private void collectSmallStars(CrystalBallNode node, int depth, float worldX, float worldY) {
    int childDepth = depth + 1;
    float length = edgeLength(childDepth);
    for (StaffDirection dir : StaffDirection.values()) {
      Vec2 edgeDir = rotatedDirection(dir, depth);
      stars.add(new Visible(node.child(dir), worldX + edgeDir.x * length, worldY + edgeDir.y * length, childDepth));
    }
  }

  private static Vec2 rotatedDirection(StaffDirection dir, int parentDepth) {
    float angle = (float) Math.toRadians(ROTATION_DEGREES) * (parentDepth % 2 == 0 ? 1 : -1);
    float cos = Mth.cos(angle);
    float sin = Mth.sin(angle);
    Vec2 base = dir.asVec2();
    return new Vec2(base.x * cos - base.y * sin, base.x * sin + base.y * cos);
  }

  private static float size(int depth) {
    return ROOT_SIZE / (1 << depth);
  }

  private static float edgeLength(int depth) {
    return ROOT_EDGE_LENGTH / (1 << (depth - 1));
  }

  private static float subtreeRadius(int depth) {
    return ROOT_EDGE_LENGTH * 2 / (1 << depth) + size(depth) / 2;
  }

  private void drawEdge(int parentDepth, float parentWorldX, float parentWorldY, CrystalBallNode child, int childDepth, float childWorldX,
      float childWorldY, Vec2 edgeDir) {
    float childPx = size(childDepth) * zoom;
    float v = visibility(childPx) * fadeLarge(size(parentDepth) * zoom);
    if (v <= 0f) {
      return;
    }
    int alpha = alpha255(v);

    float k = childPx / ROOT_SIZE;
    int core = Math.max(1, Math.round(k));
    int outline = core * 3;

    float laneStart = laneOf(size(parentDepth) * zoom);
    float laneEnd = laneOf(childPx);

    float rx = -edgeDir.y;
    float ry = edgeDir.x;
    float sideStart = parentDepth % 2 == 0 ? 1 : -1;
    float sideEnd = -sideStart;

    float ax = localX(parentWorldX) + rx * laneStart * sideStart;
    float ay = localY(parentWorldY) + ry * laneStart * sideStart;
    float bx = localX(childWorldX) + rx * laneEnd * sideEnd;
    float by = localY(childWorldY) + ry * laneEnd * sideEnd;

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

    int uMin = steep ? Mth.floor(vy0 - panY) : Mth.floor(vx0 - panX);
    int uMax = steep ? Mth.ceil(vy1 - panY) : Mth.ceil(vx1 - panX);
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
    float sizePx = size(v.depth()) * zoom;
    float fade = visibility(sizePx);
    if (fade <= 0f) {
      return;
    }
    int size = Math.round(sizePx);
    if (size < 2) {
      return;
    }
    float sx = screenX(v.worldX());
    float sy = screenY(v.worldY());

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
    pose.translate(sx, sy);
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, -size / 2, -size / 2, size, size, ARGB.color(alpha, 0xFFFFFF));
    pose.popMatrix();

    if (state == CrystalBallNodeState.UNKNOWN && sizePx >= 10 && fade >= 0.25f) {
      drawQuestionMark(font, sx, sy, sizePx, alpha);
    } else if ((state == CrystalBallNodeState.SPELL || state == CrystalBallNodeState.ROOT) && sizePx >= 10 && fade >= 0.25f) {
      states.iconOf(v.node()).ifPresent(icon -> drawIcon(icon, sx, sy, sizePx, alpha));
    }
  }

  private void drawQuestionMark(Font font, float sx, float sy, float sizePx, int alpha) {
    float scale = (sizePx / ROOT_SIZE) * 1.5f;
    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(sx, sy);
    pose.scale(scale, scale);
    graphics.centeredText(font, "?", 0, -font.lineHeight / 2, ARGB.color(alpha, QUESTION_MARK));
    pose.popMatrix();
  }

  private void drawIcon(Identifier icon, float sx, float sy, float sizePx, int alpha) {
    int size = Math.round(sizePx * ICON_SIZE_FACTOR);
    if (size < 1) {
      return;
    }
    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(sx, sy);
    graphics.blit(RenderPipelines.GUI_TEXTURED, icon, -size / 2, -size / 2, 0, 0, size, size, size, size, ARGB.color(alpha, 0xFFFFFF));
    pose.popMatrix();
  }

  private void drawStar(float worldX, float worldY, int minSize, int maxSize, boolean sparkle) {
    float sx = screenX(worldX);
    float sy = screenY(worldY);
    if (sx < vx0 - maxSize || sx > vx1 + maxSize || sy < vy0 - maxSize || sy > vy1 + maxSize) {
      return;
    }

    int hash = hashFor(worldX, worldY);
    int size = minSize + Math.round(unit(hash) * (maxSize - minSize));

    float speed = 1.2f + unit(hash >>> 8) * 1.8f;
    float phase = unit(hash >>> 16) * (float) (Math.PI * 2);
    float brightness = 0.5f + 0.5f * Mth.sin(time * speed + phase);
    int alpha = alpha255(brightness);
    int color = ARGB.color(alpha, 0xFFFFFF);

    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(sx, sy);
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

  private static int hashFor(float worldX, float worldY) {
    int h = Mth.murmurHash3Mixer(Float.floatToIntBits(worldX));
    return Mth.murmurHash3Mixer(h ^ Float.floatToIntBits(worldY));
  }

  private static float unit(int bits) {
    return (bits & 0xFF) / 255f;
  }

  private static float fadeSmall(float sizePx) {
    return Math.clamp((sizePx - FADE_OUT_PX) / (FADE_IN_PX - FADE_OUT_PX), 0, 1);
  }

  private static float fadeLarge(float sizePx) {
    return Math.clamp(1 - (sizePx - FADE_LARGE_START_PX) / (FADE_LARGE_END_PX - FADE_LARGE_START_PX), 0, 1);
  }

  private static float visibility(float sizePx) {
    return fadeSmall(sizePx) * fadeLarge(sizePx);
  }

  private static int alpha255(float fade) {
    return Math.clamp(Math.round(fade * 255), 0, 255);
  }

  private float screenX(float worldX) {
    return originX + worldX * zoom;
  }

  private float screenY(float worldY) {
    return originY + worldY * zoom;
  }

  private float localX(float worldX) {
    return centerX + worldX * zoom;
  }

  private float localY(float worldY) {
    return centerY + worldY * zoom;
  }
}
