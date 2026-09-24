package gragongit.arcaneartistry.common.crystalball;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CrystalBallPayload() implements CustomPacketPayload {
  public static final CrystalBallPayload INSTANCE = new CrystalBallPayload();

  public static final CustomPacketPayload.Type<CrystalBallPayload> TYPE =
      new CustomPacketPayload.Type<>(ArcaneArtistry.id("crystal_ball_packet"));

  public static final StreamCodec<FriendlyByteBuf, CrystalBallPayload> CODEC = StreamCodec.unit(INSTANCE);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
