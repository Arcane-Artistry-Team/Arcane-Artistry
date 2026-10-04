package gragongit.arcaneartistry.common.registry;

import java.util.function.Function;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
  private ModItems() {}

  public static final Item LAPIS_CRYSTAL = register("lapis_crystal", Item::new, new Item.Properties());

  private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
    ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ArcaneArtistry.id(name));
    return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
  }

  public static void register() {}
}
