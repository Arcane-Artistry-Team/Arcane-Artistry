package gragongit.arcaneartistry.client.guidebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import gragongit.arcaneartistry.client.guidebook.page.BookPageRenderers;
import gragongit.arcaneartistry.client.guidebook.page.DisplayPage;
import gragongit.arcaneartistry.client.guidebook.page.PageContent;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import gragongit.arcaneartistry.common.guidebook.GuideBookProgress.EntryState;
import gragongit.arcaneartistry.common.guidebook.MarkEntryReadPayload;
import gragongit.arcaneartistry.common.guidebook.page.BookPage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;

/** An entry opened as a two page spread. Pages that don't fit continue on the next spread. */
public class GuideEntryScreen extends Screen {
  private static final Identifier COVER_SPRITE = ArcaneArtistry.id("guide_book/cover");
  private static final Identifier PAGE_SPRITE = ArcaneArtistry.id("guide_book/page");

  private static final int PAGE_WIDTH = 140;
  private static final int PAGE_HEIGHT = 184;
  private static final int COVER = 8;
  private static final int SPREAD_WIDTH = 2 * PAGE_WIDTH + 2 * COVER;
  private static final int SPREAD_HEIGHT = PAGE_HEIGHT + 2 * COVER;
  private static final int PAGE_PADDING = 12;
  private static final int FOOTER_HEIGHT = 16;
  private static final int TEXT_WIDTH = PAGE_WIDTH - 2 * PAGE_PADDING;
  private static final int TEXT_HEIGHT = PAGE_HEIGHT - 2 * PAGE_PADDING - FOOTER_HEIGHT;
  private static final int SPINE_COLOR = 0x60402010;
  private static final int PAGE_NUMBER_COLOR = 0xFF8B6B4A;

  private static final int BUTTON_WIDTH = 100;
  private static final int BUTTON_HEIGHT = 20;
  private static final int PAGE_BUTTON_WIDTH = 23;
  private static final int PAGE_BUTTON_HEIGHT = 13;

  private final GuideBookScreen map;
  private final GuideBookView view;
  private final Holder.Reference<BookEntry> entry;
  private final List<DisplayPage> pages = new ArrayList<>();
  private int spread;
  private PageButton backButton;
  private PageButton forwardButton;

  public GuideEntryScreen(GuideBookScreen map, GuideBookView view, Holder.Reference<BookEntry> entry) {
    super(entry.value().title());
    this.map = map;
    this.view = view;
    this.entry = entry;
  }

  /** Opens {@code entry} if it can be read, marking it as read. */
  public static void open(GuideBookScreen map, GuideBookView view, Holder.Reference<BookEntry> entry) {
    EntryState state = view.stateOf(entry);
    if (!state.isOpenable()) {
      return;
    }
    if (state != EntryState.READ) {
      ClientPlayNetworking.send(new MarkEntryReadPayload(entry.key()));
    }
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1F));
    minecraft.gui.setScreen(new GuideEntryScreen(map, view, entry));
  }

  @Override
  protected void init() {
    pages.clear();
    Optional<Component> heading = Optional.of(entry.value().title());
    for (BookPage page : entry.value().pages()) {
      PageContent content = BookPageRenderers.content(page, view, TEXT_WIDTH);
      pages.addAll(DisplayPage.paginate(this.font, content, heading, TEXT_WIDTH, TEXT_HEIGHT));
      heading = Optional.empty();
    }
    spread = Math.min(spread, lastSpread());

    int left = left();
    int bottom = top() + SPREAD_HEIGHT;
    int buttonY = bottom - COVER - PAGE_PADDING / 2 - PAGE_BUTTON_HEIGHT;
    backButton = addRenderableWidget(new PageButton(left + COVER + PAGE_PADDING, buttonY, false, button -> turn(-1), true));
    forwardButton = addRenderableWidget(new PageButton(left + SPREAD_WIDTH - COVER - PAGE_PADDING - PAGE_BUTTON_WIDTH, buttonY, true,
        button -> turn(1), true));
    addRenderableWidget(Button
        .builder(CommonComponents.GUI_BACK, button -> onClose())
        .bounds((this.width - BUTTON_WIDTH) / 2, bottom + 4, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build());
    updateButtons();
  }

  private int left() {
    return (this.width - SPREAD_WIDTH) / 2;
  }

  private int top() {
    return (this.height - SPREAD_HEIGHT - BUTTON_HEIGHT - 4) / 2;
  }

  private int lastSpread() {
    return Math.max(0, (pages.size() - 1) / 2);
  }

  private void turn(int direction) {
    spread = Math.clamp(spread + direction, 0, lastSpread());
    updateButtons();
  }

  private void updateButtons() {
    backButton.visible = spread > 0;
    forwardButton.visible = spread < lastSpread();
  }

  private int pageX(int side) {
    return left() + COVER + side * PAGE_WIDTH + PAGE_PADDING;
  }

  private int pageY() {
    return top() + COVER + PAGE_PADDING;
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    int left = left();
    int top = top();
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, COVER_SPRITE, left, top, SPREAD_WIDTH, SPREAD_HEIGHT);
    for (int side = 0; side < 2; side++) {
      graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAGE_SPRITE, left + COVER + side * PAGE_WIDTH, top + COVER, PAGE_WIDTH, PAGE_HEIGHT);
    }
    int spine = left + SPREAD_WIDTH / 2;
    graphics.fillGradient(spine - 3, top + COVER, spine, top + COVER + PAGE_HEIGHT, 0, SPINE_COLOR);
    graphics.fillGradient(spine, top + COVER, spine + 3, top + COVER + PAGE_HEIGHT, SPINE_COLOR, 0);

    for (int side = 0; side < 2; side++) {
      int index = spread * 2 + side;
      if (index >= pages.size()) {
        continue;
      }
      pages.get(index).extract(graphics, this.font, pageX(side), pageY(), TEXT_WIDTH, mouseX, mouseY);
      String number = String.valueOf(index + 1);
      int numberY = top + COVER + PAGE_HEIGHT - PAGE_PADDING / 2 - this.font.lineHeight;
      graphics.text(this.font, number, pageX(side) + (TEXT_WIDTH - this.font.width(number)) / 2, numberY, PAGE_NUMBER_COLOR, false);
    }

    if (linkAt(mouseX, mouseY).isPresent()) {
      graphics.requestCursor(CursorTypes.POINTING_HAND);
    }
    super.extractRenderState(graphics, mouseX, mouseY, delta);
  }

  private Optional<Holder.Reference<BookEntry>> linkAt(double mouseX, double mouseY) {
    for (int side = 0; side < 2; side++) {
      int index = spread * 2 + side;
      int x = pageX(side);
      if (index >= pages.size() || mouseX < x || mouseX >= x + TEXT_WIDTH) {
        continue;
      }
      Optional<ResourceKey<BookEntry>> target = pages
          .get(index)
          .styleAt(this.font, x, pageY(), (int) mouseX, (int) mouseY)
          .flatMap(BookTextLayout::linkTarget);
      if (target.isPresent()) {
        return target.flatMap(view::entry).filter(linked -> view.stateOf(linked).isOpenable());
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (super.mouseClicked(event, doubleClick)) {
      return true;
    }
    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
      Optional<Holder.Reference<BookEntry>> link = linkAt(event.x(), event.y());
      if (link.isPresent()) {
        open(map, view, link.get());
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (scrollY != 0) {
      int before = spread;
      turn(scrollY > 0 ? -1 : 1);
      if (spread != before) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1F));
      }
    }
    return true;
  }

  /** Escape returns to the map. */
  @Override
  public void onClose() {
    this.minecraft.gui.setScreen(map);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
