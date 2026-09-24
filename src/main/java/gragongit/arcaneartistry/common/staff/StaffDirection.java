package gragongit.arcaneartistry.common.staff;

import java.util.Locale;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec2;

public enum StaffDirection {
  RIGHT, DOWN, LEFT, UP; // NOTE - Direction order is important!

  public static final Codec<StaffDirection> CODEC = Codec.STRING.comapFlatMap(name -> {
    try {
      return DataResult.success(StaffDirection.valueOf(name.toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException e) {
      return DataResult.error(() -> "Unknown staff direction: " + name);
    }
  }, direction -> direction.name().toLowerCase(Locale.ROOT));

  public static final StreamCodec<ByteBuf, StaffDirection> STREAM_CODEC =
      ByteBufCodecs.idMapper(id -> StaffDirection.values()[id], StaffDirection::ordinal);

  public StaffDirection opposite() {
    return StaffDirection.values()[(this.ordinal() + StaffDirection.values().length / 2) % StaffDirection.values().length];
  }

  public StaffDirection right() {
    return StaffDirection.values()[(this.ordinal() + 1) % StaffDirection.values().length];
  }

  public Vec2 asVec2() {
    return switch (this) {
      case RIGHT -> new Vec2(1, 0);
      case DOWN -> new Vec2(0, 1);
      case LEFT -> new Vec2(-1, 0);
      case UP -> new Vec2(0, -1);
    };
  }
}
