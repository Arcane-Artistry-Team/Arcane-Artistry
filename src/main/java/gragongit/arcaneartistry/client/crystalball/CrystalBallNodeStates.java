package gragongit.arcaneartistry.client.crystalball;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.spell.SpellsByStaffType;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public final class CrystalBallNodeStates implements CrystalBallRenderer.CrystalBallNodeStateProvider {
  private final CrystalBallTheme crystalBall;
  private final Map<CastPattern, Spell> spells;
  private final Set<CastPattern> explored;
  private final Map<CrystalBallNode, CrystalBallNodeState> cache = new IdentityHashMap<>();

  private CrystalBallNodeStates(CrystalBallTheme crystalBall, Map<CastPattern, Spell> spells, Set<CastPattern> explored) {
    this.crystalBall = crystalBall;
    this.spells = spells;
    this.explored = explored;
  }

  public static CrystalBallNodeStates forStaffType(Registry<Spell> registry, Holder<StaffType> staffType, Set<CastPattern> explored) {
    return new CrystalBallNodeStates(staffType.value().crystalBall(), SpellsByStaffType.of(registry).forStaffType(staffType), explored);
  }

  @Override
  public int connectionColor() {
    return crystalBall.connectionColor();
  }

  @Override
  public Optional<Identifier> backgroundShader() {
    return crystalBall.background();
  }

  @Override
  public CrystalBallNodeState stateOf(CrystalBallNode node) {
    return cache.computeIfAbsent(node, this::compute);
  }

  @Override
  public Optional<Identifier> iconOf(CrystalBallNode node) {
    if (node.isRoot()) {
      return Optional.of(crystalBall.icon());
    }
    return spellAt(node).map(spell -> spell.crystalBall().icon());
  }

  private CrystalBallNodeState compute(CrystalBallNode node) {
    if (node.isRoot()) {
      return CrystalBallNodeState.ROOT;
    }
    CastPattern pattern = node.path();
    if (!explored.contains(pattern)) {
      return CrystalBallNodeState.UNKNOWN;
    }
    return spells.containsKey(pattern) ? CrystalBallNodeState.SPELL : CrystalBallNodeState.EXPLORED;
  }

  public Optional<Spell> spellAt(CrystalBallNode node) {
    return Optional.ofNullable(spells.get(node.path()));
  }

  public record CrystalBallTheme(int connectionColor, Optional<Identifier> background, Identifier icon) {
    public static final int DEFAULT_CONNECTION_COLOR = 0xC0C0C0;

    public static final Codec<CrystalBallTheme> CODEC = RecordCodecBuilder
        .create(instance -> instance
            .group(
                ExtraCodecs.STRING_RGB_COLOR
                    .optionalFieldOf("connection_color", DEFAULT_CONNECTION_COLOR)
                    .forGetter(CrystalBallTheme::connectionColor),
                Identifier.CODEC.optionalFieldOf("background").forGetter(CrystalBallTheme::background),
                Identifier.CODEC.fieldOf("icon").forGetter(CrystalBallTheme::icon))
            .apply(instance, CrystalBallTheme::new));
  }

  public record CrystalBallEntry(Identifier icon) {
    public static final Codec<CrystalBallEntry> CODEC = RecordCodecBuilder
        .create(instance -> instance
            .group(Identifier.CODEC.fieldOf("icon").forGetter(CrystalBallEntry::icon))
            .apply(instance, CrystalBallEntry::new));
  }
}
