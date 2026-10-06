package gragongit.arcaneartistry.client.guidebook.page;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A large item, centered on the page. */
final class ItemVisual implements PageVisual {
  private static final int SCALE = 3;
  private static final int SIZE = 16 * SCALE;
  private static final int PADDING = 4;

  private final ItemStack stack;

  ItemVisual(Holder<Item> item) {
    this.stack = new ItemStack(item);
  }

  @Override
  public int height() {
    return SIZE + 2 * PADDING;
  }

  @Override
  public void extract(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int mouseX, int mouseY) {
    int left = x + (width - SIZE) / 2;
    int top = y + PADDING;
    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(left, top);
    pose.scale(SCALE, SCALE);
    graphics.item(stack, 0, 0);
    pose.popMatrix();
    if (mouseX >= left && mouseX < left + SIZE && mouseY >= top && mouseY < top + SIZE) {
      graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
    }
  }
}
