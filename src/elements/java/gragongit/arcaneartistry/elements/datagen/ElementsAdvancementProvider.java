package gragongit.arcaneartistry.elements.datagen;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** Hidden advancements (no display) that gate guide book entries. */
public class ElementsAdvancementProvider extends FabricAdvancementProvider {
  public static final Identifier OBTAIN_BLAZE_ROD = ArcaneArtistryElements.id("guide_book/obtain_blaze_rod");
  public static final Identifier OBTAIN_BREEZE_ROD = ArcaneArtistryElements.id("guide_book/obtain_breeze_rod");

  public ElementsAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
    output.accept(obtain(OBTAIN_BLAZE_ROD, Items.BLAZE_ROD));
    output.accept(obtain(OBTAIN_BREEZE_ROD, Items.BREEZE_ROD));
  }

  private static AdvancementHolder obtain(Identifier id, ItemLike item) {
    return Advancement.Builder.advancement().addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(item)).build(id);
  }

  @Override
  public String getName() {
    return "Arcane Artistry Elements Advancements";
  }
}
