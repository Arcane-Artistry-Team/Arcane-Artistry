package gragongit.arcaneartistry.common.crystalball;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CrystalBallPayload(Holder<StaffType> staffType, int maxPatternLength, BlockPos origin) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<CrystalBallPayload> TYPE =
      new CustomPacketPayload.Type<>(ArcaneArtistry.id("crystal_ball_packet"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CrystalBallPayload> CODEC = StreamCodec
      .composite(StaffType.STREAM_CODEC, CrystalBallPayload::staffType, ByteBufCodecs.VAR_INT, CrystalBallPayload::maxPatternLength,
          BlockPos.STREAM_CODEC, CrystalBallPayload::origin, CrystalBallPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
