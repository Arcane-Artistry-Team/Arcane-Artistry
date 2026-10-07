package gragongit.arcaneartistry.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.klikli_dev.modonomicon.client.gui.book.node.BookCategoryNodeScreen;
import gragongit.arcaneartistry.client.guidebook.ShaderCategoryBackground;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(BookCategoryNodeScreen.class)
public class BookCategoryNodeScreenMixin {
  @Shadow
  private float scrollX;
  @Shadow
  private float scrollY;

  @Unique
  private final long openedAt = System.nanoTime();

  @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
  private void renderShaderBackground(GuiGraphicsExtractor graphics, CallbackInfo ci) {
    BookCategoryNodeScreen screen = (BookCategoryNodeScreen) (Object) this;
    ShaderCategoryBackground.shaderOf(screen.getCategory().getBackground()).ifPresent(shader -> {
      float seconds = (System.nanoTime() - openedAt) / 1_000_000_000f;
      ShaderCategoryBackground.render(graphics, screen, shader, scrollX, scrollY, seconds);
      ci.cancel();
    });
  }
}
