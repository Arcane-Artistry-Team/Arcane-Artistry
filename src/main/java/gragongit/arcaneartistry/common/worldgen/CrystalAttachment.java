package gragongit.arcaneartistry.common.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

/**
 * The kind of surface a crystal grows out of.
 */
public enum CrystalAttachment implements StringRepresentable {
  WALL("wall"), FLOOR("floor"), CEILING("ceiling");

  public static final Codec<CrystalAttachment> CODEC = StringRepresentable.fromEnum(CrystalAttachment::values);

  private final String name;

  CrystalAttachment(String name) {
    this.name = name;
  }

  /**
   * The direction in which to search for the surface, a random horizontal one for walls.
   */
  public Direction searchDirection(RandomSource random) {
    return switch (this) {
      case WALL -> Direction.Plane.HORIZONTAL.getRandomDirection(random);
      case FLOOR -> Direction.DOWN;
      case CEILING -> Direction.UP;
    };
  }

  @Override
  public String getSerializedName() {
    return name;
  }
}
