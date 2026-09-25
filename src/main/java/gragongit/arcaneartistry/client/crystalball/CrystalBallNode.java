package gragongit.arcaneartistry.client.crystalball;

import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.staff.StaffDirection;

public final class CrystalBallNode {
  private final CrystalBallNode parent;
  private final CrystalBallNode[] children = new CrystalBallNode[StaffDirection.values().length];

  private CrystalBallNode(CrystalBallNode parent) {
    this.parent = parent;
  }

  public static CrystalBallNode createRoot() {
    return new CrystalBallNode(null);
  }

  public CrystalBallNode child(StaffDirection dir) {
    CrystalBallNode c = children[dir.ordinal()];
    if (c == null) {
      c = new CrystalBallNode(this);
      children[dir.ordinal()] = c;
    }
    return c;
  }

  public CrystalBallNode find(CastPattern pattern) {
    CrystalBallNode node = this;
    for (StaffDirection dir : pattern.strokes()) {
      node = node.child(dir);
    }
    return node;
  }

  public CrystalBallNode parent() {
    return parent;
  }

  public boolean isRoot() {
    return parent == null;
  }

  public StaffDirection direction() {
    if (parent == null) {
      return null;
    }
    for (StaffDirection dir : StaffDirection.values()) {
      if (parent.children[dir.ordinal()] == this) {
        return dir;
      }
    }
    throw new IllegalStateException("Parent node has no connection to this node");
  }

  public int depth() {
    int depth = 0;
    for (CrystalBallNode n = this; n.parent != null; n = n.parent) {
      depth++;
    }
    return depth;
  }

  public CastPattern path() {
    if (parent == null) {
      return CastPattern.empty();
    }
    return parent.path().add(direction());
  }
}
