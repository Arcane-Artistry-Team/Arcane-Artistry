package gragongit.arcaneartistry.common.network;

import gragongit.arcaneartistry.common.crystalball.CrystalBallPayload;
import gragongit.arcaneartistry.common.guidebook.MarkEntryReadPayload;
import gragongit.arcaneartistry.common.guidebook.OpenGuideBookPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
  public static void register() {
    PayloadTypeRegistry.serverboundPlay().register(StrokePayload.TYPE, StrokePayload.CODEC);
    PayloadTypeRegistry.serverboundPlay().register(StaffRenderOffsetPayload.TYPE, StaffRenderOffsetPayload.CODEC);
    PayloadTypeRegistry.clientboundPlay().register(CrystalBallPayload.TYPE, CrystalBallPayload.CODEC);
    PayloadTypeRegistry.serverboundPlay().register(MarkEntryReadPayload.TYPE, MarkEntryReadPayload.CODEC);
    PayloadTypeRegistry.clientboundPlay().register(OpenGuideBookPayload.TYPE, OpenGuideBookPayload.CODEC);
  }
}
