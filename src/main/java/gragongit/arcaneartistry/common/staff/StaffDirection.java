package gragongit.arcaneartistry.common.staff;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum StaffDirection {
  UP, DOWN, LEFT, RIGHT;

  public static final StreamCodec<ByteBuf, StaffDirection> STREAM_CODEC =
      ByteBufCodecs.idMapper(id -> StaffDirection.values()[id], StaffDirection::ordinal);
}
