package gragongit.arcaneartistry.common.api;

import java.util.ArrayList;
import java.util.List;
import com.mojang.serialization.Codec;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CastPattern(List<StaffDirection> strokes) {
  public static final Codec<CastPattern> CODEC = StaffDirection.CODEC.listOf().xmap(CastPattern::new, CastPattern::strokes);

  public static final StreamCodec<ByteBuf, CastPattern> STREAM_CODEC =
      StaffDirection.STREAM_CODEC.apply(ByteBufCodecs.list()).map(CastPattern::new, CastPattern::strokes);

  public CastPattern {
    strokes = List.copyOf(strokes);
  }

  public static CastPattern of(StaffDirection... strokes) {
    return new CastPattern(List.of(strokes));
  }

  public static CastPattern of(String pattern) {
    return new CastPattern(parse(pattern));
  }

  public static CastPattern empty() {
    return new CastPattern(List.of());
  }

  public CastPattern add(StaffDirection... strokes) {
    List<StaffDirection> combined = new ArrayList<>(this.strokes);
    combined.addAll(List.of(strokes));
    return new CastPattern(combined);
  }

  public CastPattern add(String pattern) {
    List<StaffDirection> combined = new ArrayList<>(this.strokes);
    combined.addAll(parse(pattern));
    return new CastPattern(combined);
  }

  private static List<StaffDirection> parse(String pattern) {
    return pattern.chars().mapToObj(c -> switch (Character.toUpperCase(c)) {
      case 'U' -> StaffDirection.UP;
      case 'D' -> StaffDirection.DOWN;
      case 'L' -> StaffDirection.LEFT;
      case 'R' -> StaffDirection.RIGHT;
      default -> throw new IllegalArgumentException("Unknown char '" + (char) c + "' in CastPattern '" + pattern + "'");
    }).toList();
  }

  public boolean isEmpty() {
    return strokes.isEmpty();
  }

  public StaffDirection getLast() {
    return strokes.getLast();
  }

  public int size() {
    return strokes.size();
  }

  @Override
  public String toString() {
    return strokes.toString();
  }
}
