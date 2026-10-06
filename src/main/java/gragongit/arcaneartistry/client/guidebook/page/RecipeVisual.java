package gragongit.arcaneartistry.client.guidebook.page;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;

/** Inputs on the left, an arrow (with the crafting station above it) and the result on the right. */
final class RecipeVisual implements PageVisual {
  private static final int SLOT = 18;
  private static final int GAP = 4;
  private static final int ARROW_WIDTH = 14;
  private static final int STATION_HEIGHT = 3 * SLOT;
  private static final int CYCLE_MILLIS = 1000;

  private static final int SLOT_BORDER = 0xFF8B6B4A;
  private static final int SLOT_FILL = 0xFFDCC69E;
  private static final int RESULT_BORDER = 0xFF5A1E0E;
  private static final int ARROW_COLOR = 0xFF7A5A3A;

  private final List<SlotDisplay> inputs;
  private final int columns;
  private final SlotDisplay result;
  private final SlotDisplay station;

  private RecipeVisual(List<SlotDisplay> inputs, int columns, SlotDisplay result, SlotDisplay station) {
    this.inputs = inputs;
    this.columns = Math.max(1, columns);
    this.result = result;
    this.station = station;
  }

  static PageVisual of(ResourceKey<Recipe<?>> id, List<RecipeDisplay> displays) {
    if (displays.isEmpty()) {
      return new MissingRecipe(id);
    }
    RecipeDisplay display = displays.getFirst();
    return switch (display) {
      case ShapedCraftingRecipeDisplay shaped -> new RecipeVisual(shaped.ingredients(), shaped.width(), shaped.result(),
          shaped.craftingStation());
      case ShapelessCraftingRecipeDisplay shapeless -> new RecipeVisual(shapeless.ingredients(), shapelessColumns(shapeless.ingredients().size()),
          shapeless.result(), shapeless.craftingStation());
      case FurnaceRecipeDisplay furnace -> new RecipeVisual(List.of(furnace.ingredient()), 1, furnace.result(), furnace.craftingStation());
      case StonecutterRecipeDisplay stonecutter -> new RecipeVisual(List.of(stonecutter.input()), 1, stonecutter.result(),
          stonecutter.craftingStation());
      case SmithingRecipeDisplay smithing -> new RecipeVisual(List.of(smithing.template(), smithing.base(), smithing.addition()), 3,
          smithing.result(), smithing.craftingStation());
      default -> new RecipeVisual(List.of(), 1, display.result(), display.craftingStation());
    };
  }

  private static int shapelessColumns(int ingredients) {
    return ingredients <= 1 ? 1 : ingredients <= 4 ? 2 : 3;
  }

  private int rows() {
    return (inputs.size() + columns - 1) / columns;
  }

  private boolean hasStation() {
    return !(station instanceof SlotDisplay.Empty);
  }

  @Override
  public int height() {
    return Math.max(Math.max(rows() * SLOT, SLOT), hasStation() ? STATION_HEIGHT : 0);
  }

  @Override
  public void extract(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int mouseX, int mouseY) {
    ContextMap context = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
    int gridWidth = inputs.isEmpty() ? 0 : columns * SLOT;
    int gridHeight = rows() * SLOT;
    int totalWidth = inputs.isEmpty() ? SLOT : gridWidth + GAP + ARROW_WIDTH + GAP + SLOT;
    int left = x + (width - totalWidth) / 2;
    int middle = y + height() / 2;

    int gridTop = middle - gridHeight / 2;
    for (int i = 0; i < inputs.size(); i++) {
      int slotX = left + (i % columns) * SLOT;
      int slotY = gridTop + (i / columns) * SLOT;
      slot(graphics, font, inputs.get(i), context, slotX, slotY, SLOT_BORDER, mouseX, mouseY);
    }

    int arrowLeft = left + gridWidth + GAP;
    if (!inputs.isEmpty()) {
      arrow(graphics, arrowLeft, middle);
    }
    if (hasStation()) {
      ItemStack stationStack = cycle(station.resolveForStacks(context));
      int stationX = arrowLeft + (ARROW_WIDTH - 16) / 2;
      int stationY = middle - 8 - 18;
      graphics.item(stationStack, stationX, stationY);
      tooltip(graphics, font, stationStack, stationX, stationY, 16, mouseX, mouseY);
    }
    int resultX = inputs.isEmpty() ? left : arrowLeft + ARROW_WIDTH + GAP;
    slot(graphics, font, result, context, resultX, middle - SLOT / 2, RESULT_BORDER, mouseX, mouseY);
  }

  private static void slot(GuiGraphicsExtractor graphics, Font font, SlotDisplay display, ContextMap context, int x, int y, int border,
      int mouseX, int mouseY) {
    graphics.fill(x, y, x + SLOT, y + SLOT, border);
    graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, SLOT_FILL);
    ItemStack stack = cycle(display.resolveForStacks(context));
    if (stack.isEmpty()) {
      return;
    }
    graphics.item(stack, x + 1, y + 1);
    graphics.itemDecorations(font, stack, x + 1, y + 1);
    tooltip(graphics, font, stack, x, y, SLOT, mouseX, mouseY);
  }

  private static void tooltip(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, int size, int mouseX, int mouseY) {
    if (!stack.isEmpty() && mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
      graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
    }
  }

  private static ItemStack cycle(List<ItemStack> stacks) {
    if (stacks.isEmpty()) {
      return ItemStack.EMPTY;
    }
    return stacks.get((int) (System.currentTimeMillis() / CYCLE_MILLIS % stacks.size()));
  }

  private static void arrow(GuiGraphicsExtractor graphics, int left, int middle) {
    int headLength = 4;
    int shaftEnd = left + ARROW_WIDTH - headLength;
    graphics.fill(left, middle - 1, shaftEnd, middle + 1, ARROW_COLOR);
    for (int i = 0; i < headLength; i++) {
      graphics.fill(shaftEnd + i, middle - headLength + i, shaftEnd + i + 1, middle + headLength - i, ARROW_COLOR);
    }
  }

  private record MissingRecipe(ResourceKey<Recipe<?>> id) implements PageVisual {
    private static final int COLOR = 0xFF8B0000;

    @Override
    public int height() {
      return 20;
    }

    @Override
    public void extract(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int mouseX, int mouseY) {
      Component text = Component.translatable("screen.arcane-artistry.guide_book.missing_recipe", id.identifier().toString());
      graphics.textWithWordWrap(font, text, x, y, width, COLOR, false);
    }
  }
}
