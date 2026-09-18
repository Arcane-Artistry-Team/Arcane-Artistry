package gragongit.arcaneartistry.common.api;

import java.util.List;
import com.mojang.serialization.Codec;
import gragongit.arcaneartistry.common.staff.StaffDirection;

public record CastPattern(List<StaffDirection> strokes) {
  public static final Codec<CastPattern> CODEC = StaffDirection.CODEC.listOf().xmap(CastPattern::new, CastPattern::strokes);

  @Override
  public boolean equals(Object obj) {
    return obj instanceof CastPattern other && strokes.equals(other.strokes);
  }

  public static CastPattern of(String pattern) {
    List<StaffDirection> strokes = pattern.chars().mapToObj(c -> switch (Character.toUpperCase(c)) {
      case 'U' -> StaffDirection.UP;
      case 'D' -> StaffDirection.DOWN;
      case 'L' -> StaffDirection.LEFT;
      case 'R' -> StaffDirection.RIGHT;
      default -> throw new IllegalArgumentException("Unknown char '" + (char) c + "' in CastPattern '" + pattern + "'");
    }).toList();
    return new CastPattern(strokes);
  }
}
