package gragongit.arcaneartistry.common.api;

import gragongit.arcaneartistry.common.staff.StaffCastAttachments;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.world.phys.Vec2;

public final class CastState {
  private final AttachmentTarget target;

  private CastState(AttachmentTarget target) {
    this.target = target;
  }

  public static CastState of(AttachmentTarget target) {
    return new CastState(target);
  }

  public boolean isCasting() {
    return target.getAttachedOrElse(StaffCastAttachments.IS_CASTING, false);
  }

  public void setCasting(boolean casting) {
    target.setAttached(StaffCastAttachments.IS_CASTING, casting);
  }

  public Vec2 getAccumulatedDelta() {
    return target.getAttachedOrElse(StaffCastAttachments.ACCUMULATED_DELTA, Vec2.ZERO);
  }

  public void setAccumulatedDelta(Vec2 delta) {
    target.setAttached(StaffCastAttachments.ACCUMULATED_DELTA, delta);
  }

  public float getStaffRenderOffsetYaw() {
    return target.getAttachedOrElse(StaffCastAttachments.STAFF_RENDER_OFFSET_YAW, 0F);
  }

  public void setStaffRenderOffsetYaw(float yaw) {
    target.setAttached(StaffCastAttachments.STAFF_RENDER_OFFSET_YAW, yaw);
  }

  public float getStaffRenderOffsetPitch() {
    return target.getAttachedOrElse(StaffCastAttachments.STAFF_RENDER_OFFSET_PITCH, 0F);
  }

  public void setStaffRenderOffsetPitch(float pitch) {
    target.setAttached(StaffCastAttachments.STAFF_RENDER_OFFSET_PITCH, pitch);
  }

  public Vec2 getStaffRenderOffsetOld() {
    return target.getAttachedOrElse(StaffCastAttachments.STAFF_RENDER_OFFSET_OLD, Vec2.ZERO);
  }

  public void setStaffRenderOffsetOld(Vec2 oldOffset) {
    target.setAttached(StaffCastAttachments.STAFF_RENDER_OFFSET_OLD, oldOffset);
  }

  public CastPattern getStrokes() {
    return target.getAttachedOrElse(StaffCastAttachments.STROKES, CastPattern.empty());
  }

  public void addStroke(StaffDirection direction) {
    target.modifyAttached(StaffCastAttachments.STROKES, current -> current.add(direction));
  }

  public void clearStrokes() {
    target.setAttached(StaffCastAttachments.STROKES, CastPattern.empty());
  }
}
