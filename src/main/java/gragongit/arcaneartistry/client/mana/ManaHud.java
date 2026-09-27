package gragongit.arcaneartistry.client.mana;

import java.util.List;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.mana.ManaState;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.player.LocalPlayer;

public final class ManaHud {
  // Shared
  private static final int FILL_COLOR = 0xFF3A7BFF;

  // Crosshair bar
  private static final int VANILLA_CROSSHAIR_SIZE = 15;
  private static final int CROSSHAIR_BAR_GAP = 2;
  private static final int CROSSHAIR_BAR_WIDTH = 1;
  private static final int CROSSHAIR_BAR_HEIGHT = VANILLA_CROSSHAIR_SIZE;
  private static final int CROSSHAIR_TRACK_COLOR = 0x40000000;

  // Corner bar: placement and size
  private static final int CORNER_BAR_MARGIN = 6;
  private static final int CORNER_BAR_WIDTH = 14;
  private static final int CORNER_BAR_BASE_HEIGHT = 84;

  // Corner bar: frame
  private static final int FRAME_THICKNESS = 1;
  private static final int FRAME_SHADE_THICKNESS = 1;
  private static final int CORNER_BAR_FRAME = FRAME_THICKNESS + FRAME_SHADE_THICKNESS;
  private static final int CORNER_CUT = 1;
  private static final int FRAME_COLOR = 0xFFE0B040;
  private static final int FRAME_SHADE_COLOR = 0xFFA07820;

  // Corner bar: contents
  private static final int CORNER_BACKGROUND_COLOR = 0xAA101020;
  private static final int FILL_HIGHLIGHT_WIDTH = 1;
  private static final int FILL_HIGHLIGHT_COLOR = 0xFF6FA0FF;

  // Corner bar: sections. The first tier whose limit is >= the player's max mana decides the section size.
  private record SectionTier(float upToMaxMana, int manaPerSection) {
  }

  private static final List<SectionTier> SECTION_TIERS =
      List.of(new SectionTier(200, 20), new SectionTier(500, 50), new SectionTier(Float.MAX_VALUE, 100));
  private static final int MIN_SECTION_HEIGHT = 3;
  private static final int DIVIDER_THICKNESS = 1;
  private static final int DIVIDER_FILLED_COLOR = 0x80000000;
  private static final int DIVIDER_EMPTY_COLOR = 0x30FFFFFF;

  // Corner bar: growth. Up to this max mana the bar keeps its base height, above it grows at the same pixels per mana.
  private static final float GROW_ABOVE_MAX_MANA = 500;
  private static final float MAX_HEIGHT_SCREEN_FRACTION = 0.5F;

  public static void register() {
    HudElementRegistry
        .attachElementAfter(VanillaHudElements.CROSSHAIR, ArcaneArtistry.id("mana_bar_crosshair"), ManaHud::extractCrosshairBar);
    HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ArcaneArtistry.id("mana_bar"), ManaHud::extractCornerBar);
  }

  private static void extractCrosshairBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    Minecraft minecraft = Minecraft.getInstance();
    if (ManaHudConfig.POSITION.get() != ManaBarPosition.CROSSHAIR || !minecraft.options.getCameraType().isFirstPerson()
        || minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR)) {
      return;
    }
    ManaState state = visibleManaState(minecraft.player);
    if (state == null) {
      return;
    }

    int left = (graphics.guiWidth() - VANILLA_CROSSHAIR_SIZE) / 2 + VANILLA_CROSSHAIR_SIZE + CROSSHAIR_BAR_GAP;
    int top = (graphics.guiHeight() - VANILLA_CROSSHAIR_SIZE) / 2;
    int right = left + CROSSHAIR_BAR_WIDTH;
    int bottom = top + CROSSHAIR_BAR_HEIGHT;
    int filled = Math.round(CROSSHAIR_BAR_HEIGHT * state.getMana() / state.getMax());

    graphics.fill(left, top, right, bottom - filled, CROSSHAIR_TRACK_COLOR);
    graphics.fill(left, bottom - filled, right, bottom, FILL_COLOR);
  }

  private static void extractCornerBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    if (ManaHudConfig.POSITION.get() != ManaBarPosition.BOTTOM_RIGHT) {
      return;
    }
    ManaState state = visibleManaState(Minecraft.getInstance().player);
    if (state == null) {
      return;
    }
    float max = state.getMax();
    float mana = state.getMana();
    int innerHeight = cornerInnerHeight(max, graphics.guiHeight());

    int right = graphics.guiWidth() - CORNER_BAR_MARGIN;
    int bottom = graphics.guiHeight() - CORNER_BAR_MARGIN;
    int left = right - CORNER_BAR_WIDTH;
    int top = bottom - innerHeight - 2 * CORNER_BAR_FRAME;

    fillBorder(graphics, left, top, right, bottom, FRAME_THICKNESS, CORNER_CUT, FRAME_COLOR);
    fillBorder(graphics, left + FRAME_THICKNESS, top + FRAME_THICKNESS, right - FRAME_THICKNESS, bottom - FRAME_THICKNESS,
        FRAME_SHADE_THICKNESS, 0, FRAME_SHADE_COLOR);

    int innerLeft = left + CORNER_BAR_FRAME;
    int innerRight = right - CORNER_BAR_FRAME;
    int innerTop = top + CORNER_BAR_FRAME;
    int innerBottom = bottom - CORNER_BAR_FRAME;
    int fillTop = innerBottom - Math.round(innerHeight * mana / max);

    graphics.fill(innerLeft, innerTop, innerRight, fillTop, CORNER_BACKGROUND_COLOR);
    graphics.fill(innerLeft, fillTop, innerRight, innerBottom, FILL_COLOR);
    graphics.fill(innerLeft, fillTop, innerLeft + FILL_HIGHLIGHT_WIDTH, innerBottom, FILL_HIGHLIGHT_COLOR);

    int step = manaPerSection(max, innerHeight);
    for (int sectionMana = step; sectionMana < max; sectionMana += step) {
      int y = innerBottom - Math.round(innerHeight * sectionMana / max);
      graphics.fill(innerLeft, y, innerRight, y + DIVIDER_THICKNESS, y >= fillTop ? DIVIDER_FILLED_COLOR : DIVIDER_EMPTY_COLOR);
    }
  }

  private static int cornerInnerHeight(float max, int guiHeight) {
    int baseHeight = CORNER_BAR_BASE_HEIGHT - 2 * CORNER_BAR_FRAME;
    if (max <= GROW_ABOVE_MAX_MANA) {
      return baseHeight;
    }
    int grownHeight = Math.round(baseHeight * max / GROW_ABOVE_MAX_MANA);
    int maxHeight = (int) (guiHeight * MAX_HEIGHT_SCREEN_FRACTION) - 2 * CORNER_BAR_FRAME;
    return Math.max(baseHeight, Math.min(grownHeight, maxHeight));
  }

  private static int manaPerSection(float max, int innerHeight) {
    int tierSize = SECTION_TIERS.stream().filter(tier -> max <= tier.upToMaxMana()).findFirst().orElseThrow().manaPerSection();
    int step = tierSize;
    while (innerHeight * step / max < MIN_SECTION_HEIGHT) {
      step += tierSize;
    }
    return step;
  }

  private static void fillBorder(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int thickness, int cornerCut,
      int color) {
    graphics.fill(left + cornerCut, top, right - cornerCut, top + thickness, color);
    graphics.fill(left + cornerCut, bottom - thickness, right - cornerCut, bottom, color);
    graphics.fill(left, top + Math.max(cornerCut, thickness), left + thickness, bottom - Math.max(cornerCut, thickness), color);
    graphics.fill(right - thickness, top + Math.max(cornerCut, thickness), right, bottom - Math.max(cornerCut, thickness), color);
  }

  private static ManaState visibleManaState(LocalPlayer player) {
    if (player == null || player.isSpectator()) {
      return null;
    }
    ManaState state = ManaState.of(player);
    float max = state.getMax();
    if (max <= 0 || (state.getMana() >= max && !isHoldingStaff(player))) {
      return null;
    }
    return state;
  }

  private static boolean isHoldingStaff(LocalPlayer player) {
    return player.getMainHandItem().has(ModDataComponents.STAFF) || player.getOffhandItem().has(ModDataComponents.STAFF);
  }
}
