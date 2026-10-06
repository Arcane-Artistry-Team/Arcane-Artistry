package gragongit.arcaneartistry.client.guidebook;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import gragongit.arcaneartistry.common.guidebook.BookCategory;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import gragongit.arcaneartistry.common.guidebook.GuideBookProgress;
import gragongit.arcaneartistry.common.guidebook.GuideBookProgress.EntryState;
import gragongit.arcaneartistry.common.guidebook.OpenGuideBookPayload;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

/** Everything the guide book screens need to know, read from the synced registries, the player and the open payload. */
public final class GuideBookView {
  private final LocalPlayer player;
  private final Registry<BookCategory> categories;
  private final Registry<BookEntry> entries;
  private final Set<ResourceKey<BookEntry>> unlocked;
  private final Map<ResourceKey<Recipe<?>>, List<RecipeDisplay>> recipes;

  public GuideBookView(LocalPlayer player, OpenGuideBookPayload payload) {
    this.player = player;
    this.categories = player.registryAccess().lookupOrThrow(ModRegistries.BOOK_CATEGORY_KEY);
    this.entries = player.registryAccess().lookupOrThrow(ModRegistries.BOOK_ENTRY_KEY);
    this.unlocked = payload.unlocked();
    this.recipes = payload.recipes();
  }

  public LocalPlayer player() {
    return player;
  }

  public EntryState stateOf(Holder.Reference<BookEntry> entry) {
    return GuideBookProgress.of(player).stateOf(entries, entry.key(), entry.value(), unlocked);
  }

  public List<Holder.Reference<BookEntry>> entriesIn(ResourceKey<BookCategory> category) {
    return entries.listElements().filter(entry -> entry.value().category().equals(category)).toList();
  }

  /** Categories that contain at least one visible entry, in display order. */
  public List<Holder.Reference<BookCategory>> visibleCategories() {
    return categories
        .listElements()
        .filter(category -> entriesIn(category.key()).stream().anyMatch(entry -> stateOf(entry).isVisible()))
        .sorted(Comparator
            .comparingInt((Holder.Reference<BookCategory> category) -> category.value().sort())
            .thenComparing(category -> category.key().identifier()))
        .toList();
  }

  public Optional<Holder.Reference<BookEntry>> entry(ResourceKey<BookEntry> key) {
    return entries.get(key);
  }

  public List<RecipeDisplay> recipe(ResourceKey<Recipe<?>> recipe) {
    return recipes.getOrDefault(recipe, List.of());
  }
}
