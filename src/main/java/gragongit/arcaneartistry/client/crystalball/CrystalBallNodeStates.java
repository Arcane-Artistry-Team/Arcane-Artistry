package gragongit.arcaneartistry.client.crystalball;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.crystalball.CrystalBallTheme;
import gragongit.arcaneartistry.common.presentation.Presentation;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.spell.SpellsByStaffType;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class CrystalBallNodeStates implements CrystalBallRenderer.NodeStateProvider {
  private final CrystalBallTheme theme;
  private final Presentation rootPresentation;
  private final Map<CastPattern, Spell> spells;
  private final Set<CastPattern> explored;
  private final Map<CrystalBallNode, CrystalBallNodeState> cache = new IdentityHashMap<>();

  private CrystalBallNodeStates(CrystalBallTheme theme, Presentation rootPresentation, Map<CastPattern, Spell> spells,
      Set<CastPattern> explored) {
    this.theme = theme;
    this.rootPresentation = rootPresentation;
    this.spells = spells;
    this.explored = explored;
  }

  public static CrystalBallNodeStates forStaffType(Registry<Spell> registry, Holder<StaffType> staffType, Set<CastPattern> explored) {
    StaffType type = staffType.value();
    return new CrystalBallNodeStates(type.crystalBallTheme(), type.presentation(),
        SpellsByStaffType.of(registry).forStaffType(staffType), explored);
  }

  @Override
  public int connectionColor() {
    return theme.connectionColor();
  }

  @Override
  public Optional<Identifier> backgroundShader() {
    return theme.background();
  }

  @Override
  public CrystalBallNodeState stateOf(CrystalBallNode node) {
    return cache.computeIfAbsent(node, this::compute);
  }

  @Override
  public Optional<Presentation> presentationOf(CrystalBallNode node) {
    if (node.isRoot()) {
      return Optional.of(rootPresentation);
    }
    return spellAt(node).map(Spell::presentation);
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
}
