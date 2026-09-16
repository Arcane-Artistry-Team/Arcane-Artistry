package gragongit.arcaneartistry.common.network;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StrokePayload(StaffDirection direction) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<StrokePayload> TYPE = new CustomPacketPayload.Type<>(ArcaneArtistry.id("stroke_packet"));

  public static final StreamCodec<RegistryFriendlyByteBuf, StrokePayload> CODEC =
      StreamCodec.composite(StaffDirection.STREAM_CODEC, StrokePayload::direction, StrokePayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
