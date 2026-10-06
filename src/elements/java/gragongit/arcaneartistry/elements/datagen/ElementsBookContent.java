package gragongit.arcaneartistry.elements.datagen;

import gragongit.arcaneartistry.common.guidebook.BookCategory;
import gragongit.arcaneartistry.datagen.guidebook.BookContent;
import gragongit.arcaneartistry.datagen.guidebook.BookContext;
import gragongit.arcaneartistry.datagen.guidebook.CoreBookContent;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;

public final class ElementsBookContent implements BookContent {
  @Override
  public String namespace() {
    return ArcaneArtistryElements.MOD_ID;
  }

  @Override
  public void define(BookContext book) {
    ResourceKey<BookCategory> elements = book
        .category("elements",
            category -> category
                .title("Elements")
                .icon(Items.BLAZE_ROD)
                .sort(10)
                .shaderBackground(ArcaneArtistryElements.id("crystal_ball/tidal_galaxy"))
                .connectionColor(0x2A78C8));

    book
        .entry("fire",
            entry -> entry
                .category(elements)
                .position(0, -1)
                .parent(CoreBookContent.STAFFS)
                .advancement(ElementsAdvancementProvider.OBTAIN_BLAZE_ROD)
                .icon(Items.BLAZE_ROD)
                .title("Fire")
                .description("Channels the raw heat of the inferno. Requires a blaze rod.")
                .showcase(Items.BLAZE_ROD, "Staff of Fire", "A blaze rod can be used as a staff of fire.")
                .text("Fireball", "Pattern: **Up, Down**\nMana: 30\n\nHurls an explosive ball of fire where you are looking."));

    book
        .entry("water",
            entry -> entry
                .category(elements)
                .position(0, 1)
                .parent(CoreBookContent.STAFFS)
                .advancement(ElementsAdvancementProvider.OBTAIN_BREEZE_ROD)
                .icon(Items.BREEZE_ROD)
                .title("Water")
                .description("Draws on the depths of the tides. Requires a breeze rod.")
                .showcase(Items.BREEZE_ROD, "Staff of Water", "A breeze rod can be used as a staff of water.")
                .text("Waterbomb", "Pattern: **Left, Right**\nMana: 15\n\nLaunches a bursting orb of water."));
  }
}
