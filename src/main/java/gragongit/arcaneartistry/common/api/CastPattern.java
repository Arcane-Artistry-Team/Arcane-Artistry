package gragongit.arcaneartistry.common.api;

import java.util.List;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class CastPattern {
  private static final int BITS_PER_STROKE = 2;
  private static final long STROKE_MASK = (1L << BITS_PER_STROKE) - 1;
  private static final long EMPTY_BITS = 1L;
  private static final StaffDirection[] DIRECTIONS = StaffDirection.values();

  public static final int MAX_LENGTH = (Long.SIZE - 1) / BITS_PER_STROKE;

  private static final CastPattern EMPTY = new CastPattern(EMPTY_BITS);

  public static final Codec<CastPattern> CODEC = StaffDirection.CODEC
      .listOf()
      .comapFlatMap(strokes -> strokes.size() <= MAX_LENGTH ? DataResult.success(of(strokes))
          : DataResult.error(() -> "CastPattern has " + strokes.size() + " strokes, max is " + MAX_LENGTH), CastPattern::strokes);

  public static final StreamCodec<ByteBuf, CastPattern> STREAM_CODEC = ByteBufCodecs.VAR_LONG.map(bits -> {
    if (!isValid(bits)) {
      throw new DecoderException("Invalid CastPattern bits: " + Long.toBinaryString(bits));
    }
    return new CastPattern(bits);
  }, pattern -> pattern.bits);

  private final long bits;

  private CastPattern(long bits) {
    this.bits = bits;
  }

  public static CastPattern empty() {
    return EMPTY;
  }

  public static CastPattern of(StaffDirection... strokes) {
    return EMPTY.add(strokes);
  }

  public static CastPattern of(List<StaffDirection> strokes) {
    return EMPTY.add(strokes.toArray(StaffDirection[]::new));
  }

  public static CastPattern of(String pattern) {
    return EMPTY.add(pattern);
  }

  public CastPattern add(StaffDirection stroke) {
    checkCapacity(1);
    return new CastPattern(append(bits, stroke));
  }

  public CastPattern add(StaffDirection... strokes) {
    checkCapacity(strokes.length);
    long b = bits;
    for (StaffDirection stroke : strokes) {
      b = append(b, stroke);
    }
    return new CastPattern(b);
  }

  public CastPattern add(String pattern) {
    return add(parse(pattern));
  }

  private static StaffDirection[] parse(String pattern) {
    StaffDirection[] strokes = new StaffDirection[pattern.length()];
    for (int i = 0; i < strokes.length; i++) {
      char c = pattern.charAt(i);
      strokes[i] = switch (Character.toUpperCase(c)) {
        case 'U' -> StaffDirection.UP;
        case 'D' -> StaffDirection.DOWN;
        case 'L' -> StaffDirection.LEFT;
        case 'R' -> StaffDirection.RIGHT;
        default -> throw new IllegalArgumentException("Unknown char '" + c + "' in CastPattern '" + pattern + "'");
      };
    }
    return strokes;
  }

  private void checkCapacity(int additional) {
    if (size() + additional > MAX_LENGTH) {
      throw new IllegalArgumentException("CastPattern exceeds max length of " + MAX_LENGTH + " strokes");
    }
  }

  private static long append(long bits, StaffDirection stroke) {
    return (bits << BITS_PER_STROKE) | stroke.ordinal();
  }

  private static boolean isValid(long bits) {
    return bits > 0 && sentinelPosition(bits) % BITS_PER_STROKE == 0;
  }

  private static int sentinelPosition(long bits) {
    return Long.SIZE - 1 - Long.numberOfLeadingZeros(bits);
  }

  public boolean isEmpty() {
    return bits == EMPTY_BITS;
  }

  public int size() {
    return sentinelPosition(bits) / BITS_PER_STROKE;
  }

  public StaffDirection get(int index) {
    int size = size();
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Index " + index + " out of bounds for CastPattern of size " + size);
    }
    return DIRECTIONS[(int) ((bits >>> ((size - 1 - index) * BITS_PER_STROKE)) & STROKE_MASK)];
  }

  public StaffDirection getLast() {
    if (isEmpty()) {
      throw new IllegalStateException("CastPattern is empty");
    }
    return DIRECTIONS[(int) (bits & STROKE_MASK)];
  }

  public List<StaffDirection> strokes() {
    StaffDirection[] strokes = new StaffDirection[size()];
    for (int i = 0; i < strokes.length; i++) {
      strokes[i] = get(i);
    }
    return List.of(strokes);
  }

  public String toArrows() {
    return strokes().stream().map(stroke -> String.valueOf(stroke.arrow())).collect(Collectors.joining(" "));
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof CastPattern other && other.bits == bits;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(bits);
  }

  @Override
  public String toString() {
    return strokes().toString();
  }
}
