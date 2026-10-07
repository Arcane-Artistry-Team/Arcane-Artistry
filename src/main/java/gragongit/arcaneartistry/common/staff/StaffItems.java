package gragongit.arcaneartistry.common.staff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class StaffItems {
  private static final Map<ResourceKey<StaffType>, List<Item>> BY_TYPE = new LinkedHashMap<>();

  private StaffItems() {}

  public static void register(Item item, ResourceKey<StaffType> type) {
    BY_TYPE.computeIfAbsent(type, key -> new ArrayList<>()).add(item);
    DefaultItemComponentEvents.MODIFY
        .register(context -> context
            .modify(item, (builder, lookupProvider, modified) -> builder
                .set(ModDataComponents.STAFF, new Staff(lookupProvider.lookupOrThrow(ModRegistries.STAFF_TYPE_KEY).getOrThrow(type)))));
  }

  public static List<Item> of(ResourceKey<StaffType> type) {
    return List.copyOf(BY_TYPE.getOrDefault(type, List.of()));
  }
}
