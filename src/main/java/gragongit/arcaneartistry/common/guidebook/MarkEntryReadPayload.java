package gragongit.arcaneartistry.common.guidebook;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

public record MarkEntryReadPayload(ResourceKey<BookEntry> entry) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<MarkEntryReadPayload> TYPE =
      new CustomPacketPayload.Type<>(ArcaneArtistry.id("mark_book_entry_read"));

  public static final StreamCodec<ByteBuf, MarkEntryReadPayload> CODEC =
      ResourceKey.streamCodec(ModRegistries.BOOK_ENTRY_KEY).map(MarkEntryReadPayload::new, MarkEntryReadPayload::entry);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
