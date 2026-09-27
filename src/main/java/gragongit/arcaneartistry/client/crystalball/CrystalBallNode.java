package gragongit.arcaneartistry.client.crystalball;

import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.staff.StaffDirection;

public final class CrystalBallNode {
  private final CrystalBallNode parent;
  private final CastPattern path;
  private final CrystalBallNode[] children = new CrystalBallNode[StaffDirection.values().length];

  private CrystalBallNode(CrystalBallNode parent, CastPattern path) {
    this.parent = parent;
    this.path = path;
  }

  public static CrystalBallNode createRoot() {
    return new CrystalBallNode(null, CastPattern.empty());
  }

  public CrystalBallNode child(StaffDirection dir) {
    CrystalBallNode c = children[dir.ordinal()];
    if (c == null) {
      c = new CrystalBallNode(this, path.add(dir));
      children[dir.ordinal()] = c;
    }
    return c;
  }

  public CrystalBallNode find(CastPattern pattern) {
    CrystalBallNode node = this;
    for (int i = 0; i < pattern.size(); i++) {
      node = node.child(pattern.get(i));
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
    return parent == null ? null : path.getLast();
  }

  public int depth() {
    return path.size();
  }

  public CastPattern path() {
    return path;
  }
}
