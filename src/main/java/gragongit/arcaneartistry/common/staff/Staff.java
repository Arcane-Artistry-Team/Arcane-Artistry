package gragongit.arcaneartistry.common.staff;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record Staff(StaffType type) {
  public static final Codec<Staff> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(ModRegistries.STAFF_TYPES.byNameCodec().fieldOf("type").forGetter(Staff::type))
          .apply(instance, Staff::new));

  public static boolean is(ItemStack stack) {
    return stack.has(ModDataComponents.STAFF);
  }

  public static boolean is(Player player) {
    return is(player.getUseItem());
  }

  public static Staff get(ItemStack stack) {
    return stack.get(ModDataComponents.STAFF);
  }

  public static Staff get(Player player) {
    return get(player.getUseItem());
  }
}
