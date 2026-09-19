package gragongit.arcaneartistry.common.staff;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record Staff(Holder<StaffType> type) {

  public static final Codec<Staff> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(RegistryFileCodec.create(ModRegistries.STAFF_TYPE_KEY, StaffType.CODEC).fieldOf("staff_type").forGetter(Staff::type))
          .apply(instance, Staff::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, Staff> STREAM_CODEC = StaffType.STREAM_CODEC.map(Staff::new, Staff::type);

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
