package gragongit.arcaneartistry.common.crystalball;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.mojang.serialization.Codec;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffType;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public final class CrystalBallAttachments {
  private static final StreamCodec<FriendlyByteBuf, Set<CastPattern>> PATTERN_SET_STREAM_CODEC =
      ByteBufCodecs.collection(HashSet::new, CastPattern.STREAM_CODEC);

  private static final StreamCodec<ByteBuf, ResourceKey<StaffType>> STAFF_TYPE_KEY_STREAM_CODEC =
      ResourceKey.streamCodec(ModRegistries.STAFF_TYPE_KEY);

  private static final Codec<Set<CastPattern>> PATTERN_SET_CODEC = CastPattern.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

  private static final Codec<Map<ResourceKey<StaffType>, Set<CastPattern>>> EXPLORED_CODEC =
      Codec.unboundedMap(ResourceKey.codec(ModRegistries.STAFF_TYPE_KEY), PATTERN_SET_CODEC);

  private static final StreamCodec<FriendlyByteBuf, Map<ResourceKey<StaffType>, Set<CastPattern>>> EXPLORED_STREAM_CODEC =
      ByteBufCodecs.map(HashMap::new, STAFF_TYPE_KEY_STREAM_CODEC, PATTERN_SET_STREAM_CODEC);

  public static final AttachmentType<Map<ResourceKey<StaffType>, Set<CastPattern>>> EXPLORED_PATTERNS = AttachmentRegistry
      .create(ArcaneArtistry.id("explored_patterns"),
          builder -> builder
              .initializer(HashMap::new)
              .persistent(EXPLORED_CODEC)
              .syncWith(EXPLORED_STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

  public static void register() {}
}
