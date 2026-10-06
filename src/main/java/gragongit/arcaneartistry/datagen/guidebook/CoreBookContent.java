package gragongit.arcaneartistry.datagen.guidebook;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.guidebook.BookCategory;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import gragongit.arcaneartistry.common.registry.ModBlocks;
import gragongit.arcaneartistry.common.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;

public final class CoreBookContent implements BookContent {
  /** Other modules can hang their entries below this one. */
  public static final ResourceKey<BookEntry> STAFFS = BookContext.entryKey(ArcaneArtistry.id("staffs"));

  @Override
  public String namespace() {
    return ArcaneArtistry.MOD_ID;
  }

  @Override
  public void define(BookContext book) {
    ResourceKey<BookCategory> basics = book
        .category("basics", category -> category.title("Arcane Basics").icon(ModItems.GUIDE_BOOK).sort(0).connectionColor(0x6B4A2B));

    ResourceKey<BookEntry> guideBook = book
        .entry("guide_book",
            entry -> entry
                .category(basics)
                .position(0, 0)
                .icon(ModItems.GUIDE_BOOK)
                .title("The Arcane Compendium")
                .description("A record of everything you have learned about the arcane.")
                .text("Welcome",
                    "This compendium records your arcane studies. Every symbol on the map is an *entry*; click one to read it.\n\n"
                        + "New entries appear once you have read the entries they build upon. Entries that are {#8B0000}locked{} still "
                        + "need something from you before they can be read.")
                .recipe(Identifier.withDefaultNamespace("book"),
                    "The compendium is made from an ordinary book. Use one on an awakened "
                        + "[Crystal Ball](entry:arcane-artistry:crystal_ball) to bind it."));

    ResourceKey<BookEntry> orbRing = book
        .entry("orb_ring",
            entry -> entry
                .category(basics)
                .position(3, -1)
                .parent(guideBook)
                .icon(ModBlocks.ORB_RING.asItem())
                .title("Orb Ring")
                .description("A golden cradle for a crystal.")
                .showcase(ModBlocks.ORB_RING.asItem(), "Orb Ring",
                    "The orb ring has to be placed on top of a sturdy block. On its own it does nothing, but it can hold a "
                        + "[Lapis Crystal](entry:arcane-artistry:lapis_crystal)."));

    ResourceKey<BookEntry> lapisCrystal = book
        .entry("lapis_crystal",
            entry -> entry
                .category(basics)
                .position(3, 1)
                .parent(guideBook)
                .icon(ModItems.LAPIS_CRYSTAL)
                .title("Lapis Crystal")
                .description("A crystal humming with arcane potential.")
                .showcase(ModItems.LAPIS_CRYSTAL, "Lapis Crystal",
                    "Using a lapis crystal on an [Orb Ring](entry:arcane-artistry:orb_ring) sets it into the ring, creating a "
                        + "**Crystal Ball**."));

    ResourceKey<BookEntry> crystalBall = book
        .entry("crystal_ball",
            entry -> entry
                .category(basics)
                .position(6, 0)
                .parent(orbRing)
                .parent(lapisCrystal)
                .icon(Items.ENDER_EYE)
                .title("Crystal Ball")
                .description("Gaze into the patterns of your staff.")
                .text("Crystal Ball",
                    "Use a [staff](entry:arcane-artistry:staffs) on a crystal ball to look into its patterns. Every pattern you have "
                        + "cast with that staff is revealed there, and patterns that form a spell are marked.\n\n"
                        + "Drag to look around, scroll to zoom and click a pattern to focus it.")
                .text("Binding a Compendium", "Using a book on a crystal ball binds it into a new compendium."));

    ResourceKey<BookEntry> staffs = book
        .entry("staffs",
            entry -> entry
                .category(basics)
                .position(9, 0)
                .parent(crystalBall)
                .icon(Items.BLAZE_ROD)
                .title("Staffs")
                .description("Channel spells through motion.")
                .text("Casting",
                    "Some items can serve as a staff. Hold *use* with one to begin casting, then move the mouse in strokes: "
                        + "**up**, **down**, **left** or **right**. Let go to finish the pattern.\n\n"
                        + "If the pattern matches a spell of that staff, the spell is cast. Every pattern you try is remembered by "
                        + "the [Crystal Ball](entry:arcane-artistry:crystal_ball)."));

    book
        .entry("mana",
            entry -> entry
                .category(basics)
                .position(12, 0)
                .parent(staffs)
                .icon(Items.LAPIS_LAZULI)
                .title("Mana")
                .description("The price of every spell.")
                .text("Mana",
                    "Every spell costs mana, and the mana bar shows how much you have left. Mana regenerates over time: slowly at "
                        + "first, then faster the longer you go without casting.\n\n"
                        + "Without enough mana for a spell, the spell fizzles."));
  }
}
