package gragongit.arcaneartistry.common.staff;

import java.util.Locale;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum StaffDirection {
  RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT, UP, UP_RIGHT; // NOTE - Direction order is important!

  public static final Codec<StaffDirection> CODEC = Codec.STRING.comapFlatMap(name -> {
    try {
      return DataResult.success(StaffDirection.valueOf(name.toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException e) {
      return DataResult.error(() -> "Unknown staff direction: " + name);
    }
  }, direction -> direction.name().toLowerCase(Locale.ROOT));

  public static final StreamCodec<ByteBuf, StaffDirection> STREAM_CODEC =
      ByteBufCodecs.idMapper(id -> StaffDirection.values()[id], StaffDirection::ordinal);
}
