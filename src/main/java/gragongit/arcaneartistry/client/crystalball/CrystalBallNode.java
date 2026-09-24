package gragongit.arcaneartistry.client.crystalball;

import java.util.List;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import net.minecraft.world.phys.Vec2;

public final class CrystalBallNode {
  public static final int MAX_DEPTH = 8;
  public static final float ROOT_SIZE = 26;
  public static final float ROOT_EDGE_LENGTH = ROOT_SIZE * 8;

  private final CrystalBallNode parent;
  private final CrystalBallNode[] children = new CrystalBallNode[StaffDirection.values().length];
  private final int depth;
  private final StaffDirection lastDirection;
  private final Vec2 coords;

  private CrystalBallNode() {
    this.parent = null;
    this.lastDirection = null;
    this.depth = 0;
    this.coords = Vec2.ZERO;
  }

  private CrystalBallNode(CrystalBallNode parent, StaffDirection direction) {
    this.parent = parent;
    this.lastDirection = direction;
    this.depth = parent.depth + 1;
    float length = edgeLength(this.depth);
    this.coords = parent.coords.add(direction.asVec2().scale(length));
  }

  public static CrystalBallNode createRoot() {
    return new CrystalBallNode();
  }

  public static float edgeLength(int depth) {
    return ROOT_EDGE_LENGTH / (1 << (depth - 1));
  }

  public CrystalBallNode child(StaffDirection dir) {
    if (depth >= MAX_DEPTH) {
      return null;
    }
    CrystalBallNode c = children[dir.ordinal()];
    if (c == null) {
      c = new CrystalBallNode(this, dir);
      children[dir.ordinal()] = c;
    }
    return c;
  }

  public CrystalBallNode find(List<StaffDirection> pattern) {
    CrystalBallNode node = this;
    for (StaffDirection dir : pattern) {
      node = node.child(dir);
      if (node == null) {
        return null;
      }
    }
    return node;
  }

  public float size() {
    return ROOT_SIZE / (1 << depth);
  }

  public float subtreeRadius() {
    return ROOT_EDGE_LENGTH * 2 / (1 << depth) + size() / 2;
  }

  public CrystalBallNode parent() {
    return parent;
  }

  public StaffDirection direction() {
    return lastDirection;
  }

  public int depth() {
    return depth;
  }

  public Vec2 coords() {
    return coords;
  }
}
