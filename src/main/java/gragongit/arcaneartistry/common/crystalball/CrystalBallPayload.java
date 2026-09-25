package gragongit.arcaneartistry.common.crystalball;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CrystalBallPayload(Holder<StaffType> staffType) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<CrystalBallPayload> TYPE =
      new CustomPacketPayload.Type<>(ArcaneArtistry.id("crystal_ball_packet"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CrystalBallPayload> CODEC =
      StaffType.STREAM_CODEC.map(CrystalBallPayload::new, CrystalBallPayload::staffType);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
