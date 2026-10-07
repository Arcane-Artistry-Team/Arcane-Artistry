package gragongit.arcaneartistry.client.crystalball;

import java.util.List;
import gragongit.arcaneartistry.client.guidebook.GuideBookMarkdown;
import gragongit.arcaneartistry.common.presentation.Presentation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

final class CrystalBallNodeHover {
  private static final Identifier TITLE_BOX_SPRITE = Identifier.withDefaultNamespace("advancements/title_box");
  private static final Identifier BOX_SPRITE = Identifier.withDefaultNamespace("advancements/box_obtained");
  private static final int MIN_FRAME_SIZE = 26;
  private static final int FRAME_MARGIN = 3;
  private static final int TITLE_PADDING_RIGHT = 5;
  private static final int FRAME_TUCK = TITLE_PADDING_RIGHT - FRAME_MARGIN;
  private static final int TITLE_PADDING_TOP = 9;
  private static final int TITLE_PADDING_BOTTOM = 8;
  private static final int TITLE_MAX_WIDTH = 163;
  private static final int TITLE_MIN_WIDTH = 80;
  private static final int DESCRIPTION_PADDING = 6;
  private static final int[] TEST_SPLIT_OFFSETS = {0, 10, -10, 25, -25};

  private record Layout(Presentation presentation, CrystalBallNodeState state, int frameSize, List<FormattedCharSequence> titleLines,
      List<FormattedCharSequence> description, int titleWidth, int descriptionWidth) {

    int textWidth() {
      return Math.max(titleWidth, descriptionWidth);
    }

    int behindFrameWidth() {
      return Math.max(frameSize + FRAME_MARGIN + titleWidth, descriptionWidth) + FRAME_MARGIN + TITLE_PADDING_RIGHT;
    }
  }

  private Layout layout;

  void extract(GuiGraphicsExtractor graphics, Font font, Presentation presentation, CrystalBallNodeState state,
      CrystalBallRenderer.HoveredNode node, int viewBottom, int screenWidth) {
    int frameSize = Math.max(Math.round(node.sizePx()), MIN_FRAME_SIZE);
    // Up to the size of a focused node the box follows vanilla: it starts behind the frame and the description sits below it.
    // Zoomed in further, the description would end up inside the frame, so the box moves beside the frame instead.
    int maxBehindFrameSize = Math.max(Math.round(node.focusSizePx()), MIN_FRAME_SIZE);
    boolean besideFrame = frameSize > maxBehindFrameSize;
    Layout layout = layout(font, presentation, state, Math.min(frameSize, maxBehindFrameSize));
    int frameLeft = (int) Math.round(node.screenX() - frameSize / 2.0);
    int frameTop = (int) Math.round(node.screenY() - frameSize / 2.0);
    int frameRight = frameLeft + frameSize;
    int width = besideFrame ? layout.textWidth() + 2 * TITLE_PADDING_RIGHT : layout.behindFrameWidth();

    int titleTextHeight = font.lineHeight * layout.titleLines().size();
    int titleBarHeight = titleTextHeight + TITLE_PADDING_TOP + TITLE_PADDING_BOTTOM;
    if (!besideFrame) {
      titleBarHeight = Math.max(titleBarHeight, frameSize);
    }
    int titleTop = frameTop + (frameSize - titleBarHeight) / 2;
    int titleTextTop = titleTop + (titleBarHeight - titleTextHeight + 1) / 2;
    int titleBarBottom = titleTop + titleBarHeight;
    int descriptionTextHeight = layout.description().size() * font.lineHeight;
    int descriptionHeight = DESCRIPTION_PADDING + descriptionTextHeight;
    boolean topSide = titleBarBottom + descriptionHeight >= viewBottom;
    boolean leftSide;
    int titleLeft;
    if (besideFrame) {
      leftSide = frameRight - FRAME_TUCK + width + MIN_FRAME_SIZE >= screenWidth;
      titleLeft = leftSide ? frameLeft + FRAME_TUCK - width : frameRight - FRAME_TUCK;
    } else {
      int x = frameLeft - FRAME_MARGIN;
      leftSide = x + width + frameSize >= screenWidth;
      titleLeft = leftSide ? x - width + frameSize + 2 * FRAME_MARGIN : x;
    }

    int backgroundHeight = titleBarHeight + descriptionHeight;
    if (!layout.description().isEmpty()) {
      int backgroundTop = topSide ? titleBarBottom - backgroundHeight : titleTop;
      graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TITLE_BOX_SPRITE, titleLeft, backgroundTop, width, backgroundHeight);
    }
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BOX_SPRITE, titleLeft, titleTop, width, titleBarHeight);
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CrystalBallRenderer.frameSprite(state), frameLeft, frameTop, frameSize, frameSize);

    int descriptionLeft = titleLeft + TITLE_PADDING_RIGHT;
    int titleTextLeft = leftSide || besideFrame ? descriptionLeft : frameRight + FRAME_MARGIN;
    extractMultilineText(graphics, font, layout.titleLines(), titleTextLeft, titleTextTop, -1);
    int descriptionTop = topSide ? titleTop - descriptionTextHeight + 1 : titleBarBottom;
    extractMultilineText(graphics, font, layout.description(), descriptionLeft, descriptionTop, -1);

    int iconSize = Math.round(frameSize * CrystalBallRenderer.ICON_SIZE_FACTOR);
    int iconLeft = frameLeft + (frameSize - iconSize) / 2;
    int iconTop = frameTop + (frameSize - iconSize) / 2;
    graphics.blit(RenderPipelines.GUI_TEXTURED, presentation.icon(), iconLeft, iconTop, 0, 0, iconSize, iconSize, iconSize, iconSize, -1);
  }

  private Layout layout(Font font, Presentation presentation, CrystalBallNodeState state, int frameSize) {
    if (layout != null && layout.presentation() == presentation && layout.state() == state && layout.frameSize() == frameSize) {
      return layout;
    }
    List<FormattedCharSequence> titleLines = font.split(presentation.title(), TITLE_MAX_WIDTH);
    int titleWidth = Math.max(titleLines.stream().mapToInt(font::width).max().orElse(0), TITLE_MIN_WIDTH);
    List<FormattedCharSequence> description = presentation
        .description()
        .map(text -> Language
            .getInstance()
            .getVisualOrder(findOptimalLines(font, GuideBookMarkdown.render(text, Style.EMPTY.withColor(descriptionColor(state))),
                frameSize + FRAME_MARGIN + titleWidth)))
        .orElse(List.of());
    int descriptionWidth = description.stream().mapToInt(font::width).max().orElse(0);
    layout = new Layout(presentation, state, frameSize, titleLines, description, titleWidth, descriptionWidth);
    return layout;
  }

  private static ChatFormatting descriptionColor(CrystalBallNodeState state) {
    return state == CrystalBallNodeState.SPELL ? ChatFormatting.DARK_PURPLE : ChatFormatting.GREEN;
  }

  private static List<FormattedText> findOptimalLines(Font font, Component input, int preferredWidth) {
    StringSplitter splitter = font.getSplitter();
    List<FormattedText> bestSplit = null;
    float bestDistance = Float.MAX_VALUE;
    for (int testMargin : TEST_SPLIT_OFFSETS) {
      List<FormattedText> testSplit = splitter.splitLines(input, preferredWidth - testMargin, Style.EMPTY);
      float distance = Math.abs(maxWidth(splitter, testSplit) - preferredWidth);
      if (distance <= 10) {
        return testSplit;
      }
      if (distance < bestDistance) {
        bestDistance = distance;
        bestSplit = testSplit;
      }
    }
    return bestSplit;
  }

  private static float maxWidth(StringSplitter splitter, List<FormattedText> lines) {
    return (float) lines.stream().mapToDouble(splitter::stringWidth).max().orElse(0);
  }

  private static void extractMultilineText(GuiGraphicsExtractor graphics, Font font, List<FormattedCharSequence> lines, int x, int y,
      int color) {
    for (int i = 0; i < lines.size(); i++) {
      graphics.text(font, lines.get(i), x, y + i * font.lineHeight, color);
    }
  }
}
