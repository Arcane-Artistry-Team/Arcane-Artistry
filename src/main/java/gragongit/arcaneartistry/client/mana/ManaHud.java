package gragongit.arcaneartistry.client.mana;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.mana.ManaState;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public final class ManaHud {
  private static final int HOTBAR_HALF_WIDTH = 91;
  private static final int HOTBAR_HEIGHT = 22;
  private static final int BAR_X_GAP = 4;
  private static final int BAR_WIDTH = 4;
  private static final int BAR_HEIGHT = HOTBAR_HEIGHT;
  private static final int BAR_BORDER = 1;
  private static final int BAR_COLOR = 0xFF3A7BFF;
  private static final int BAR_BACKGROUND_COLOR = 0xAA101020;

  public static void register() {
    HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ArcaneArtistry.id("mana_bar"), ManaHud::extractRenderState);
  }

  private static void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || player.isSpectator()) {
      return;
    }

    ManaState state = ManaState.of(player);
    float max = state.getMax();
    float mana = state.getMana();
    if (max <= 0 || (mana >= max && !isHoldingStaff(player))) {
      return;
    }

    int left = graphics.guiWidth() / 2 + HOTBAR_HALF_WIDTH + BAR_X_GAP;
    int bottom = graphics.guiHeight();
    int top = bottom - BAR_HEIGHT;
    graphics.fill(left, top, left + BAR_WIDTH, bottom, BAR_BACKGROUND_COLOR);

    int innerHeight = BAR_HEIGHT - 2 * BAR_BORDER;
    int filled = Math.round(innerHeight * mana / max);
    int innerBottom = bottom - BAR_BORDER;
    graphics.fill(left + BAR_BORDER, innerBottom - filled, left + BAR_WIDTH - BAR_BORDER, innerBottom, BAR_COLOR);
  }

  private static boolean isHoldingStaff(LocalPlayer player) {
    return player.getMainHandItem().has(ModDataComponents.STAFF) || player.getOffhandItem().has(ModDataComponents.STAFF);
  }
}
