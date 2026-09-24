package gragongit.arcaneartistry.common.network;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.staff.StaffCastAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec2;

public record StaffRenderOffsetPayload(Vec2 offset) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<StaffRenderOffsetPayload> TYPE =
      new CustomPacketPayload.Type<>(ArcaneArtistry.id("staff_render_offset"));

  public static final StreamCodec<FriendlyByteBuf, StaffRenderOffsetPayload> CODEC =
      StaffCastAttachments.VEC2_STREAM_CODEC.map(StaffRenderOffsetPayload::new, StaffRenderOffsetPayload::offset);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
