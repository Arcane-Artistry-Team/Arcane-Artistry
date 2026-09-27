package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import gragongit.arcaneartistry.client.mana.ManaHudConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {
  private static final Component ARCANE_ARTISTRY_HEADER =
      Component.translatable("options.arcane-artistry.header").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);

  private VideoSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
    super(lastScreen, options, title);
  }

  @Inject(method = "addOptions", at = @At("TAIL"))
  private void addArcaneArtistryOptions(CallbackInfo ci) {
    list.addHeader(ARCANE_ARTISTRY_HEADER);
    list.addSmall(ManaHudConfig.OPTIONS);
  }
}
