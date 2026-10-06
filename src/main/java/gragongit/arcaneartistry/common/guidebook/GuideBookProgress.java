package gragongit.arcaneartistry.common.guidebook;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class GuideBookProgress {
  public enum EntryState {
    /** Not all parents have been read yet. */
    HIDDEN,
    /** Parents are read, but the required advancement is missing. */
    LOCKED,
    AVAILABLE,
    READ;

    public boolean isVisible() {
      return this != HIDDEN;
    }

    public boolean isOpenable() {
      return this == AVAILABLE || this == READ;
    }
  }

  private final AttachmentTarget target;

  private GuideBookProgress(AttachmentTarget target) {
    this.target = target;
  }

  public static GuideBookProgress of(AttachmentTarget target) {
    return new GuideBookProgress(target);
  }

  public Set<ResourceKey<BookEntry>> getRead() {
    return target.getAttachedOrElse(GuideBookAttachments.READ_ENTRIES, Set.of());
  }

  public void markRead(ResourceKey<BookEntry> entry) {
    if (getRead().contains(entry)) {
      return;
    }
    target.modifyAttached(GuideBookAttachments.READ_ENTRIES, current -> {
      Set<ResourceKey<BookEntry>> copy = new HashSet<>(current != null ? current : Set.of());
      copy.add(entry);
      return Set.copyOf(copy);
    });
  }

  /**
   * @param unlocked entries whose advancement requirement is met, see {@link GuideBookHandler#unlockedEntries}
   */
  public EntryState stateOf(Registry<BookEntry> entries, ResourceKey<BookEntry> key, BookEntry entry, Set<ResourceKey<BookEntry>> unlocked) {
    Set<ResourceKey<BookEntry>> read = getRead();
    // Parents that no longer exist (e.g. removed by a data pack) must not hide their children forever.
    boolean parentsRead = entry.parents().stream().allMatch(parent -> read.contains(parent) || !entries.containsKey(parent));
    if (!parentsRead) {
      return EntryState.HIDDEN;
    }
    if (!unlocked.contains(key)) {
      return EntryState.LOCKED;
    }
    return read.contains(key) ? EntryState.READ : EntryState.AVAILABLE;
  }
}
