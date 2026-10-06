package gragongit.arcaneartistry.common.guidebook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import gragongit.arcaneartistry.common.guidebook.page.RecipePage;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public final class GuideBookHandler {
  private GuideBookHandler() {}

  public static void register() {
    ServerPlayNetworking
        .registerGlobalReceiver(MarkEntryReadPayload.TYPE,
            (payload, context) -> context.server().execute(() -> markRead(context.player(), payload.entry())));
  }

  public static void open(ServerPlayer player) {
    Registry<BookEntry> entries = player.registryAccess().lookupOrThrow(ModRegistries.BOOK_ENTRY_KEY);
    ServerPlayNetworking.send(player, new OpenGuideBookPayload(unlockedEntries(player, entries), recipeDisplays(player, entries)));
  }

  private static void markRead(ServerPlayer player, ResourceKey<BookEntry> key) {
    Registry<BookEntry> entries = player.registryAccess().lookupOrThrow(ModRegistries.BOOK_ENTRY_KEY);
    Optional<Holder.Reference<BookEntry>> entry = entries.get(key);
    if (entry.isEmpty()) {
      return;
    }
    GuideBookProgress progress = GuideBookProgress.of(player);
    Set<ResourceKey<BookEntry>> unlocked = isAdvancementDone(player, entry.get().value()) ? Set.of(key) : Set.of();
    if (progress.stateOf(entries, key, entry.get().value(), unlocked).isOpenable()) {
      progress.markRead(key);
    }
  }

  /** Entries whose advancement requirement is met. Entries without an advancement are always included. */
  public static Set<ResourceKey<BookEntry>> unlockedEntries(ServerPlayer player, Registry<BookEntry> entries) {
    Set<ResourceKey<BookEntry>> unlocked = new HashSet<>();
    entries.listElements().forEach(entry -> {
      if (isAdvancementDone(player, entry.value())) {
        unlocked.add(entry.key());
      }
    });
    return unlocked;
  }

  private static boolean isAdvancementDone(ServerPlayer player, BookEntry entry) {
    if (entry.advancement().isEmpty()) {
      return true;
    }
    Identifier id = entry.advancement().get();
    AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
    return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
  }

  private static Map<ResourceKey<Recipe<?>>, List<RecipeDisplay>> recipeDisplays(ServerPlayer player, Registry<BookEntry> entries) {
    RecipeManager recipes = player.level().getServer().getRecipeManager();
    Map<ResourceKey<Recipe<?>>, List<RecipeDisplay>> displays = new HashMap<>();
    entries
        .stream()
        .flatMap(entry -> entry.pages().stream())
        .filter(RecipePage.class::isInstance)
        .map(page -> ((RecipePage) page).recipe())
        .distinct()
        .forEach(key -> recipes.byKey(key).ifPresent(recipe -> displays.put(key, recipe.value().display())));
    return displays;
  }
}
