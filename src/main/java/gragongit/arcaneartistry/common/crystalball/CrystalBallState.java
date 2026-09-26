package gragongit.arcaneartistry.common.crystalball;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.resources.ResourceKey;

public final class CrystalBallState {
  public static final int MAX_PATTERN_LENGTH = 8;

  private final AttachmentTarget target;

  private CrystalBallState(AttachmentTarget target) {
    this.target = target;
  }

  public static CrystalBallState of(AttachmentTarget target) {
    return new CrystalBallState(target);
  }

  public Set<CastPattern> getExplored(ResourceKey<StaffType> staffType) {
    return target.getAttachedOrElse(CrystalBallAttachments.EXPLORED_PATTERNS, Map.of()).getOrDefault(staffType, Set.of());
  }

  public static boolean isStorable(CastPattern pattern) {
    return !pattern.isEmpty() && pattern.size() <= MAX_PATTERN_LENGTH;
  }

  public void addExplored(ResourceKey<StaffType> staffType, CastPattern pattern) {
    if (!isStorable(pattern)) {
      return;
    }
    target.modifyAttached(CrystalBallAttachments.EXPLORED_PATTERNS, current -> {
      Map<ResourceKey<StaffType>, Set<CastPattern>> base = current != null ? current : Map.of();
      Map<ResourceKey<StaffType>, Set<CastPattern>> copy = new HashMap<>(base);
      Set<CastPattern> patterns = new HashSet<>(copy.getOrDefault(staffType, Set.of()));
      patterns.add(pattern);
      copy.put(staffType, Set.copyOf(patterns));
      return copy;
    });
  }
}
