package gragongit.arcaneartistry.common.staff;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.api.CastPattern;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class StaffCastAttachments {

  public static final AttachmentType<Boolean> IS_CASTING = AttachmentRegistry
      .create(ArcaneArtistry.id("is_casting"),
          builder -> builder.initializer(() -> false).syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.allButTarget()));

  public static final AttachmentType<Double> ACCUMULATED_YAW =
      AttachmentRegistry.create(ArcaneArtistry.id("accumulated_yaw"), builder -> builder.initializer(() -> 0.0));

  public static final AttachmentType<Double> ACCUMULATED_PITCH =
      AttachmentRegistry.create(ArcaneArtistry.id("accumulated_pitch"), builder -> builder.initializer(() -> 0.0));

  public static final AttachmentType<Float> STAFF_RENDER_OFFSET_YAW = AttachmentRegistry
      .create(ArcaneArtistry.id("staff_render_offset_yaw"),
          builder -> builder.initializer(() -> 0F).syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.allButTarget()));

  public static final AttachmentType<Float> STAFF_RENDER_OFFSET_PITCH = AttachmentRegistry
      .create(ArcaneArtistry.id("staff_render_offset_pitch"),
          builder -> builder.initializer(() -> 0F).syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.allButTarget()));

  public static final AttachmentType<CastPattern> STROKES = AttachmentRegistry.create(ArcaneArtistry.id("strokes"));

  public static void register() {}
}
