package gragongit.arcaneartistry.common.network;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StaffRenderOffsetPayload(double yaw, double pitch) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<StaffRenderOffsetPayload> TYPE =
      new CustomPacketPayload.Type<>(ArcaneArtistry.id("staff_render_offset"));

  public static final StreamCodec<FriendlyByteBuf, StaffRenderOffsetPayload> CODEC = StreamCodec
      .composite(ByteBufCodecs.DOUBLE, StaffRenderOffsetPayload::yaw, ByteBufCodecs.DOUBLE, StaffRenderOffsetPayload::pitch,
          StaffRenderOffsetPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
