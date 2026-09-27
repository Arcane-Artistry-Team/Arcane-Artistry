package gragongit.arcaneartistry.common.mana;

import com.mojang.serialization.Codec;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ManaAttachments {
  public static final AttachmentType<Float> MANA = AttachmentRegistry
      .create(ArcaneArtistry.id("mana"),
          builder -> builder.persistent(Codec.FLOAT).syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

  public static final AttachmentType<Integer> TICKS_SINCE_CAST =
      AttachmentRegistry.create(ArcaneArtistry.id("ticks_since_cast"), builder -> builder.persistent(Codec.INT));

  public static void register() {}
}
