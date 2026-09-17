package gragongit.arcaneartistry.common.api;

import java.util.List;
import com.mojang.serialization.Codec;
import gragongit.arcaneartistry.common.staff.StaffDirection;

public record CastPattern(List<StaffDirection> pattern) {
  public static final Codec<CastPattern> CODEC = StaffDirection.CODEC.listOf().xmap(CastPattern::new, CastPattern::pattern);

  @Override
  public boolean equals(Object obj) {
    return obj instanceof CastPattern other && pattern.equals(other.pattern);
  }
}
