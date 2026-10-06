package gragongit.arcaneartistry.common.guidebook;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mojang.serialization.Codec;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public final class GuideBookAttachments {
  public static final StreamCodec<ByteBuf, Set<ResourceKey<BookEntry>>> ENTRY_SET_STREAM_CODEC =
      ByteBufCodecs.collection(HashSet::new, ResourceKey.streamCodec(ModRegistries.BOOK_ENTRY_KEY));

  private static final Codec<Set<ResourceKey<BookEntry>>> ENTRY_SET_CODEC =
      ResourceKey.codec(ModRegistries.BOOK_ENTRY_KEY).listOf().xmap(Set::copyOf, List::copyOf);

  public static final AttachmentType<Set<ResourceKey<BookEntry>>> READ_ENTRIES = AttachmentRegistry
      .create(ArcaneArtistry.id("read_book_entries"),
          builder -> builder
              .initializer(Set::of)
              .persistent(ENTRY_SET_CODEC)
              .syncWith(ENTRY_SET_STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

  public static void register() {}
}
