package gragongit.arcaneartistry.common.api;

import gragongit.arcaneartistry.common.staff.StaffCastAttachments;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

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

  public double getAccumulatedYaw() {
    return target.getAttachedOrElse(StaffCastAttachments.ACCUMULATED_YAW, 0.0);
  }

  public void setAccumulatedYaw(double yaw) {
    target.setAttached(StaffCastAttachments.ACCUMULATED_YAW, yaw);
  }

  public double getAccumulatedPitch() {
    return target.getAttachedOrElse(StaffCastAttachments.ACCUMULATED_PITCH, 0.0);
  }

  public void setAccumulatedPitch(double pitch) {
    target.setAttached(StaffCastAttachments.ACCUMULATED_PITCH, pitch);
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
