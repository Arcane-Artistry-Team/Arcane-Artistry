package gragongit.arcaneartistry.client.guidebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import gragongit.arcaneartistry.client.crystalball.CrystalBallBackground;
import gragongit.arcaneartistry.client.gui.MapCamera;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.guidebook.BookBackground;
import gragongit.arcaneartistry.common.guidebook.BookCategory;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import gragongit.arcaneartistry.common.guidebook.GuideBookProgress.EntryState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** The book's map view: category tabs on the left, and the entries of the selected category as a pannable, zoomable map. */
public class GuideBookScreen extends Screen {
  private static final Identifier FRAME_SPRITE = ArcaneArtistry.id("guide_book/frame");
  private static final Identifier TAB_FIRST = Identifier.withDefaultNamespace("advancements/tab_left_top");
  private static final Identifier TAB_FIRST_SELECTED = Identifier.withDefaultNamespace("advancements/tab_left_top_selected");
  private static final Identifier TAB = Identifier.withDefaultNamespace("advancements/tab_left_middle");
  private static final Identifier TAB_SELECTED = Identifier.withDefaultNamespace("advancements/tab_left_middle_selected");
  private static final Identifier FRAME_READ = Identifier.withDefaultNamespace("advancements/task_frame_obtained");
  private static final Identifier FRAME_UNREAD = Identifier.withDefaultNamespace("advancements/goal_frame_obtained");
  private static final Identifier FRAME_LOCKED = Identifier.withDefaultNamespace("advancements/task_frame_unobtained");

  private static final int MARGIN = 12;
  private static final int FRAME_BORDER = 8;
  private static final int TAB_WIDTH = 32;
  private static final int TAB_HEIGHT = 28;
  private static final int TAB_OVERLAP = 4;
  private static final int TAB_ICON_X = 10;
  private static final int TAB_ICON_Y = 5;

  private static final int CELL = 30;
  private static final int NODE_SIZE = 26;
  private static final int ICON_SIZE = 16;
  private static final int PAN_MARGIN = 2 * CELL;
  private static final double MIN_ZOOM = 0.5;
  private static final double MAX_ZOOM = 2.5;
  private static final double ZOOM_STEP = 1.2;
  private static final double CLICK_DRAG_TOLERANCE = 3;

  private static final int CONNECTION_WIDTH = 2;
  private static final int LOCKED_OVERLAY = 0x90202020;
  private static final int UNREAD_GLOW = 0xFFE8B84A;
  private static final int TEXTURE_TILE = 64;
  private static final double BACKGROUND_PARALLAX = 0.5;
  private static final double SHADER_RADIUS = 300;

  private record Node(Holder.Reference<BookEntry> entry, EntryState state, double worldX, double worldY) {
  }

  private final GuideBookView view;
  private final MapCamera camera = new MapCamera();
  private final long openedAt = System.nanoTime();
  private List<Holder.Reference<BookCategory>> categories = List.of();
  private Holder.@Nullable Reference<BookCategory> category;
  private final List<Node> nodes = new ArrayList<>();
  private int frameX0, frameY0, frameX1, frameY1;
  private int mapX0, mapY0, mapX1, mapY1;
  private boolean dragging;
  private double dragDistance;

  public GuideBookScreen(GuideBookView view) {
    super(Component.translatable("screen.arcane-artistry.guide_book"));
    this.view = view;
  }

  @Override
  protected void init() {
    frameX0 = MARGIN + TAB_WIDTH - TAB_OVERLAP;
    frameY0 = MARGIN;
    frameX1 = this.width - MARGIN;
    frameY1 = this.height - MARGIN;
    mapX0 = frameX0 + FRAME_BORDER;
    mapY0 = frameY0 + FRAME_BORDER;
    mapX1 = frameX1 - FRAME_BORDER;
    mapY1 = frameY1 - FRAME_BORDER;

    categories = view.visibleCategories();
    ResourceKey<BookCategory> wanted = category != null ? category.key() : GuideBookClientState.lastCategory;
    Holder.Reference<BookCategory> selected = categories
        .stream()
        .filter(candidate -> candidate.key().equals(wanted))
        .findFirst()
        .orElse(categories.isEmpty() ? null : categories.getFirst());
    boolean switched = category == null || selected == null || !selected.key().equals(category.key());
    category = selected;
    collectNodes();
    camera.setZoomRange(MIN_ZOOM, MAX_ZOOM);
    camera.setFocusBounds(this::clampFocus);
    if (switched) {
      restoreCamera();
    }
  }

  private void selectCategory(Holder.Reference<BookCategory> selected) {
    if (category != null && category.key().equals(selected.key())) {
      return;
    }
    saveCamera();
    category = selected;
    collectNodes();
    camera.setFocusBounds(this::clampFocus);
    restoreCamera();
  }

  private void saveCamera() {
    if (category != null) {
      GuideBookClientState.CAMERAS
          .put(category.key(), new GuideBookClientState.CameraState(camera.focusX(), camera.focusY(), camera.zoom()));
      GuideBookClientState.lastCategory = category.key();
    }
  }

  private void restoreCamera() {
    GuideBookClientState.CameraState saved = category == null ? null : GuideBookClientState.CAMERAS.get(category.key());
    if (saved != null) {
      camera.jumpTo(saved.focusX(), saved.focusY(), saved.zoom());
      return;
    }
    // Start centered on the visible entries.
    double minX = nodes.stream().mapToDouble(Node::worldX).min().orElse(0);
    double maxX = nodes.stream().mapToDouble(Node::worldX).max().orElse(0);
    double minY = nodes.stream().mapToDouble(Node::worldY).min().orElse(0);
    double maxY = nodes.stream().mapToDouble(Node::worldY).max().orElse(0);
    camera.jumpTo((minX + maxX) / 2, (minY + maxY) / 2, 1);
  }

  private MapCamera.Position clampFocus(double x, double y, double zoom) {
    if (nodes.isEmpty()) {
      return new MapCamera.Position(0, 0);
    }
    double minX = nodes.stream().mapToDouble(Node::worldX).min().orElseThrow() - PAN_MARGIN;
    double maxX = nodes.stream().mapToDouble(Node::worldX).max().orElseThrow() + PAN_MARGIN;
    double minY = nodes.stream().mapToDouble(Node::worldY).min().orElseThrow() - PAN_MARGIN;
    double maxY = nodes.stream().mapToDouble(Node::worldY).max().orElseThrow() + PAN_MARGIN;
    return new MapCamera.Position(Math.clamp(x, minX, maxX), Math.clamp(y, minY, maxY));
  }

  /** Rebuilds the visible nodes, picking up entries that were read in the meantime. */
  private void collectNodes() {
    nodes.clear();
    if (category == null) {
      return;
    }
    for (Holder.Reference<BookEntry> entry : view.entriesIn(category.key())) {
      EntryState state = view.stateOf(entry);
      if (state.isVisible()) {
        nodes.add(new Node(entry, state, entry.value().x() * CELL, entry.value().y() * CELL));
      }
    }
  }

  @Override
  public void removed() {
    saveCamera();
  }

  @Override
  public void tick() {
    collectNodes();
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    camera.update();
    if (category == null) {
      super.extractRenderState(graphics, mouseX, mouseY, delta);
      return;
    }

    graphics.enableScissor(mapX0, mapY0, mapX1, mapY1);
    extractMapBackground(graphics, category.value());
    int connectionColor = ARGB.opaque(category.value().connectionColor());
    for (Node node : nodes) {
      for (ResourceKey<BookEntry> parent : node.entry().value().parents()) {
        nodes
            .stream()
            .filter(candidate -> candidate.entry().key().equals(parent))
            .findFirst()
            .ifPresent(parentNode -> extractConnection(graphics, parentNode, node, connectionColor));
      }
    }
    for (Node node : nodes) {
      extractNode(graphics, node);
    }
    graphics.disableScissor();

    // Locked nodes are greyed out by an overlay, which must be drawn after the item icons.
    graphics.nextStratum();
    graphics.enableScissor(mapX0, mapY0, mapX1, mapY1);
    for (Node node : nodes) {
      if (node.state() == EntryState.LOCKED) {
        int half = nodeSize() / 2;
        graphics.fill(screenX(node.worldX()) - half, screenY(node.worldY()) - half, screenX(node.worldX()) + half,
            screenY(node.worldY()) + half, LOCKED_OVERLAY);
      }
    }
    graphics.disableScissor();

    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FRAME_SPRITE, frameX0, frameY0, frameX1 - frameX0, frameY1 - frameY0);
    extractTabs(graphics);
    Component title = category.value().title();
    graphics.textWithBackdrop(this.font, title, mapX0 + 6, mapY0 + 6, this.font.width(title), -1);
    super.extractRenderState(graphics, mouseX, mouseY, delta);

    Optional<Node> hovered = nodeAt(mouseX, mouseY);
    hovered.ifPresent(node -> graphics.setComponentTooltipForNextFrame(this.font, tooltip(node), mouseX, mouseY));
    Optional<Holder.Reference<BookCategory>> hoveredTab = tabAt(mouseX, mouseY);
    hoveredTab.ifPresent(tab -> graphics.setTooltipForNextFrame(this.font, tab.value().title(), mouseX, mouseY));
    if (hovered.filter(node -> node.state().isOpenable()).isPresent() || hoveredTab.isPresent()) {
      graphics.requestCursor(CursorTypes.POINTING_HAND);
    }
  }

  private void extractMapBackground(GuiGraphicsExtractor graphics, BookCategory category) {
    double offsetX = -camera.focusX() * camera.zoom() * BACKGROUND_PARALLAX;
    double offsetY = -camera.focusY() * camera.zoom() * BACKGROUND_PARALLAX;
    switch (category.background()) {
      case BookBackground.Texture texture -> {
        int startX = mapX0 + Math.floorMod((int) Math.round(offsetX), TEXTURE_TILE) - TEXTURE_TILE;
        int startY = mapY0 + Math.floorMod((int) Math.round(offsetY), TEXTURE_TILE) - TEXTURE_TILE;
        for (int x = startX; x < mapX1; x += TEXTURE_TILE) {
          for (int y = startY; y < mapY1; y += TEXTURE_TILE) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture.texture(), x, y, 0, 0, TEXTURE_TILE, TEXTURE_TILE, TEXTURE_TILE, TEXTURE_TILE);
          }
        }
      }
      case BookBackground.Shader shader -> {
        double rootX = (mapX0 + mapX1) / 2.0 + offsetX;
        double rootY = (mapY0 + mapY1) / 2.0 + offsetY;
        float seconds = (System.nanoTime() - openedAt) / 1_000_000_000f;
        CrystalBallBackground
            .render(graphics, shader.shader(), mapX0, mapY0, mapX1, mapY1, rootX, rootY, Math.sqrt(camera.zoom()), SHADER_RADIUS, seconds,
                rootX, rootY, CrystalBallBackground.NO_REVEAL_MASK);
      }
    }
  }

  /** Draws an elbow connection like the advancement screen: across to the middle, then up or down, then across again. */
  private void extractConnection(GuiGraphicsExtractor graphics, Node from, Node to, int color) {
    int ax = screenX(from.worldX());
    int ay = screenY(from.worldY());
    int bx = screenX(to.worldX());
    int by = screenY(to.worldY());
    int middleX = (ax + bx) / 2;
    int lineColor = to.state() == EntryState.LOCKED ? ARGB.color(128, color) : color;
    int outline = ARGB.color(ARGB.alpha(lineColor), ARGB.scaleRGB(color, 0.4f));
    for (int pass = 0; pass < 2; pass++) {
      int grow = pass == 0 ? 1 : 0;
      int c = pass == 0 ? outline : lineColor;
      horizontal(graphics, ax, middleX, ay, grow, c);
      vertical(graphics, middleX, ay, by, grow, c);
      horizontal(graphics, middleX, bx, by, grow, c);
    }
  }

  private static void horizontal(GuiGraphicsExtractor graphics, int x0, int x1, int y, int grow, int color) {
    int half = CONNECTION_WIDTH / 2;
    graphics.fill(Math.min(x0, x1) - half - grow, y - half - grow, Math.max(x0, x1) + half + grow, y + half + grow, color);
  }

  private static void vertical(GuiGraphicsExtractor graphics, int x, int y0, int y1, int grow, int color) {
    int half = CONNECTION_WIDTH / 2;
    graphics.fill(x - half - grow, Math.min(y0, y1) - half - grow, x + half + grow, Math.max(y0, y1) + half + grow, color);
  }

  private void extractNode(GuiGraphicsExtractor graphics, Node node) {
    int size = nodeSize();
    int x = screenX(node.worldX()) - size / 2;
    int y = screenY(node.worldY()) - size / 2;
    if (node.state() == EntryState.AVAILABLE) {
      float pulse = 0.5f + 0.5f * Mth.sin((System.nanoTime() - openedAt) / 1_000_000_000f * 4);
      int glow = Math.round(size * 0.15f) + 1;
      graphics.fill(x - glow, y - glow, x + size + glow, y + size + glow, ARGB.color(Math.round(60 + 100 * pulse), UNREAD_GLOW));
    }
    Identifier frame = switch (node.state()) {
      case AVAILABLE -> FRAME_UNREAD;
      case READ -> FRAME_READ;
      default -> FRAME_LOCKED;
    };
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, frame, x, y, size, size);
    float iconScale = (float) camera.zoom();
    var pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(screenX(node.worldX()) - ICON_SIZE * iconScale / 2, screenY(node.worldY()) - ICON_SIZE * iconScale / 2);
    pose.scale(iconScale, iconScale);
    graphics.fakeItem(new ItemStack(node.entry().value().icon()), 0, 0);
    pose.popMatrix();
  }

  private List<Component> tooltip(Node node) {
    BookEntry entry = node.entry().value();
    List<Component> lines = new ArrayList<>();
    lines.add(entry.title());
    entry.description().ifPresent(description -> lines.add(description.copy().withStyle(ChatFormatting.GRAY)));
    switch (node.state()) {
      case LOCKED -> lines.add(Component.translatable("screen.arcane-artistry.guide_book.locked").withStyle(ChatFormatting.DARK_RED));
      case AVAILABLE -> lines.add(Component.translatable("screen.arcane-artistry.guide_book.unread").withStyle(ChatFormatting.GOLD));
      default -> {}
    }
    return lines;
  }

  private void extractTabs(GuiGraphicsExtractor graphics) {
    for (int i = 0; i < categories.size(); i++) {
      Holder.Reference<BookCategory> tab = categories.get(i);
      boolean selected = category != null && tab.key().equals(category.key());
      Identifier sprite = i == 0 ? (selected ? TAB_FIRST_SELECTED : TAB_FIRST) : (selected ? TAB_SELECTED : TAB);
      graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, tabX(), tabY(i), TAB_WIDTH, TAB_HEIGHT);
      graphics.fakeItem(new ItemStack(tab.value().icon()), tabX() + TAB_ICON_X, tabY(i) + TAB_ICON_Y);
    }
  }

  private int tabX() {
    return frameX0 - TAB_WIDTH + TAB_OVERLAP;
  }

  private int tabY(int index) {
    return frameY0 + index * TAB_HEIGHT;
  }

  private Optional<Holder.Reference<BookCategory>> tabAt(double mouseX, double mouseY) {
    if (mouseX < tabX() || mouseX >= tabX() + TAB_WIDTH - TAB_OVERLAP) {
      return Optional.empty();
    }
    int index = Mth.floor((mouseY - frameY0) / TAB_HEIGHT);
    return index >= 0 && index < categories.size() ? Optional.of(categories.get(index)) : Optional.empty();
  }

  private Optional<Node> nodeAt(double mouseX, double mouseY) {
    if (mouseX < mapX0 || mouseX >= mapX1 || mouseY < mapY0 || mouseY >= mapY1 || camera.isFlying()) {
      return Optional.empty();
    }
    double half = nodeSize() / 2.0;
    for (int i = nodes.size() - 1; i >= 0; i--) {
      Node node = nodes.get(i);
      if (Math.abs(mouseX - screenX(node.worldX())) <= half && Math.abs(mouseY - screenY(node.worldY())) <= half) {
        return Optional.of(node);
      }
    }
    return Optional.empty();
  }

  private int nodeSize() {
    return Math.max(2, (int) Math.round(NODE_SIZE * camera.zoom()));
  }

  private int screenX(double worldX) {
    return (int) Math.round((mapX0 + mapX1) / 2.0 + (worldX - camera.focusX()) * camera.zoom());
  }

  private int screenY(double worldY) {
    return (int) Math.round((mapY0 + mapY1) / 2.0 + (worldY - camera.focusY()) * camera.zoom());
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (super.mouseClicked(event, doubleClick)) {
      return true;
    }
    if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
      return false;
    }
    Optional<Holder.Reference<BookCategory>> tab = tabAt(event.x(), event.y());
    if (tab.isPresent()) {
      if (category == null || !tab.get().key().equals(category.key())) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1F));
        selectCategory(tab.get());
      }
      return true;
    }
    if (event.x() >= mapX0 && event.x() < mapX1 && event.y() >= mapY0 && event.y() < mapY1) {
      dragging = true;
      dragDistance = 0;
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    if (!dragging) {
      return super.mouseDragged(event, dx, dy);
    }
    dragDistance += Math.abs(dx) + Math.abs(dy);
    camera.drag(dx, dy);
    return true;
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && dragging) {
      dragging = false;
      if (dragDistance <= CLICK_DRAG_TOLERANCE) {
        nodeAt(event.x(), event.y()).ifPresent(node -> GuideEntryScreen.open(this, view, node.entry()));
      }
      return true;
    }
    return super.mouseReleased(event);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (scrollY != 0 && !camera.isFlying()) {
      camera
          .zoomAt(mouseX - (mapX0 + mapX1) / 2.0, mouseY - (mapY0 + mapY1) / 2.0, Math.pow(ZOOM_STEP, Math.signum(scrollY)));
    }
    return true;
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    if (super.keyPressed(event)) {
      return true;
    }
    if (this.minecraft.options.keyInventory.matches(event)) {
      onClose();
      return true;
    }
    return false;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
