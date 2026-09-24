package gragongit.arcaneartistry.client.crystalball;

import java.util.ArrayList;
import java.util.List;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class CrystalBallRenderer {

  @FunctionalInterface
  public interface ChrystalBallNodeStateProvider {
    CrystalBallNodeState stateOf(CrystalBallNode node);
  }

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

  private final List<CrystalBallNode> visible = new ArrayList<>();

  private GuiGraphicsExtractor graphics;
  private ChrystalBallNodeStateProvider states;
  private int vx0, vy0, vx1, vy1;
  private float originX, originY, zoom;

  public void render(GuiGraphicsExtractor graphics, Font font, CrystalBallNode root, CrystalBallCamera camera,
      ChrystalBallNodeStateProvider states, int x0, int y0, int x1, int y1) {
    this.graphics = graphics;
    this.states = states;
    this.vx0 = x0;
    this.vy0 = y0;
    this.vx1 = x1;
    this.vy1 = y1;
    this.originX = (x0 + x1) / 2 + camera.panX();
    this.originY = (y0 + y1) / 2 + camera.panY();
    this.zoom = camera.zoom();

    visible.clear();
    graphics.enableScissor(x0, y0, x1, y1);
    collect(root);
    for (CrystalBallNode node : visible) {
      drawNode(font, node);
    }
    graphics.disableScissor();

    visible.clear();
    this.graphics = null;
    this.states = null;
  }

  private void collect(CrystalBallNode node) {
    float sizePx = node.size() * zoom;
    if (fadeSmall(sizePx) <= 0f) {
      return;
    }
    float sx = screenX(node.coords().x);
    float sy = screenY(node.coords().y);

    float r = node.subtreeRadius() * zoom;
    if (sx + r < vx0 || sx - r > vx1 || sy + r < vy0 || sy - r > vy1) {
      return;
    }
    float half = sizePx / 2;
    if (sx + half >= vx0 && sx - half <= vx1 && sy + half >= vy0 && sy - half <= vy1) {
      visible.add(node);
    }

    for (StaffDirection dir : StaffDirection.values()) {
      CrystalBallNode child = node.child(dir);
      if (child == null || fadeSmall(child.size() * zoom) <= 0f) {
        continue;
      }
      drawEdge(node, child);
      collect(child);
    }
  }

  private void drawEdge(CrystalBallNode parent, CrystalBallNode child) {
    float childPx = child.size() * zoom;
    float v = visibility(childPx) * fadeLarge(parent.size() * zoom);
    if (v <= 0f) {
      return;
    }
    int alpha = alpha255(v);

    float k = childPx / CrystalBallNode.ROOT_SIZE;
    int core = Math.max(1, (int) Math.round(k));
    int outline = core * 3;
    float lane = Math.max(2, LANE_FACTOR * core);

    StaffDirection right = child.direction().right();
    float ox = right.asVec2().x * lane;
    float oy = right.asVec2().y * lane;

    float ax = screenX(parent.coords().x) + ox;
    float ay = screenY(parent.coords().y) + oy;
    float bx = screenX(child.coords().x) + ox;
    float by = screenY(child.coords().y) + oy;

    int coreColor = states.stateOf(child) == CrystalBallNodeState.UNKNOWN ? LINE_UNKNOWN : LINE_KNOWN;
    fillLine(ax, ay, bx, by, outline, ARGB.color(alpha, LINE_OUTLINE));
    fillLine(ax, ay, bx, by, core, ARGB.color(alpha, coreColor));
  }

  private void fillLine(float ax, float ay, float bx, float by, int width, int color) {
    float half = width / 2;
    int x0 = clampX(Math.min(ax, bx) - half);
    int y0 = clampY(Math.min(ay, by) - half);
    int x1 = clampX(Math.max(ax, bx) + half);
    int y1 = clampY(Math.max(ay, by) + half);
    if (x1 > x0 && y1 > y0) {
      graphics.fill(x0, y0, x1, y1, color);
    }
  }

  private void drawNode(Font font, CrystalBallNode node) {
    float sizePx = node.size() * zoom;
    float fade = visibility(sizePx);
    if (fade <= 0f) {
      return;
    }
    int size = (int) Math.round(sizePx);
    if (size < 2) {
      return;
    }
    float sx = screenX(node.coords().x);
    float sy = screenY(node.coords().y);
    int px = (int) Math.round(sx - sizePx / 2.0);
    int py = (int) Math.round(sy - sizePx / 2.0);

    CrystalBallNodeState state = states.stateOf(node);
    Identifier sprite = switch (state) {
      case UNKNOWN -> FRAME_UNKNOWN;
      case EXPLORED -> FRAME_EXPLORED;
      case SPELL -> FRAME_SPELL;
    };
    int alpha = alpha255(fade);
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, px, py, size, size, ARGB.color(alpha, 0xFFFFFF));

    if (state == CrystalBallNodeState.UNKNOWN && sizePx >= 10.0 && fade >= 0.25f) {
      float scale = (sizePx / CrystalBallNode.ROOT_SIZE) * 1.5f;
      var pose = graphics.pose();
      pose.pushMatrix();
      pose.translate(sx, sy);
      pose.scale(scale, scale);
      graphics.centeredText(font, "?", 0, -font.lineHeight / 2, ARGB.color(alpha, QUESTION_MARK));
      pose.popMatrix();
    }
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

  private int clampX(float v) {
    return (int) Math.round(Math.clamp(v, vx0, vx1));
  }

  private int clampY(float v) {
    return (int) Math.round(Math.clamp(v, vy0, vy1));
  }
}
