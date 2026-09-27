package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import gragongit.arcaneartistry.client.ArcaneArtistryClientConfig;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

@Mixin(MouseSettingsScreen.class)
public abstract class MouseSettingsScreenMixin extends OptionsSubScreen {
  private MouseSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
    super(lastScreen, options, title);
  }

  @Inject(method = "addOptions", at = @At("TAIL"))
  private void addArcaneArtistryOptions(CallbackInfo ci) {
    list.addHeader(ArcaneArtistryClientConfig.SECTION_HEADER);
    list.addSmall(ArcaneArtistryClientConfig.MOUSE_OPTIONS);
  }
}
