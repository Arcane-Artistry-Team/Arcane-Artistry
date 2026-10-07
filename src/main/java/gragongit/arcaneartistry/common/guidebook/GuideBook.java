package gragongit.arcaneartistry.common.guidebook;

import com.klikli_dev.modonomicon.registry.DataComponentRegistry;
import com.klikli_dev.modonomicon.registry.ItemRegistry;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class GuideBook {
  public static final Identifier ID = ArcaneArtistry.id("guide_book");

  private GuideBook() {}

  public static ItemStack createStack() {
    ItemStack stack = new ItemStack(ItemRegistry.MODONOMICON.get());
    stack.set(DataComponentRegistry.BOOK_ID.get(), ID);
    return stack;
  }
}
