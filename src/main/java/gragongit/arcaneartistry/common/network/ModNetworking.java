package gragongit.arcaneartistry.common.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
  public static void register() {
    PayloadTypeRegistry.serverboundPlay().register(StrokePayload.TYPE, StrokePayload.CODEC);
  }
}
