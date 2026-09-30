package gragongit.arcaneartistry.client.crystalball;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.crystalball.CrystalBallEntry;
import gragongit.arcaneartistry.common.crystalball.CrystalBallTheme;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.spell.SpellsByStaffType;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class CrystalBallNodeStates implements CrystalBallRenderer.NodeStateProvider {
  private final CrystalBallTheme theme;
  private final CrystalBallEntry rootEntry;
  private final Map<CastPattern, Spell> spells;
  private final Set<CastPattern> explored;
  private final Map<CrystalBallNode, CrystalBallNodeState> cache = new IdentityHashMap<>();

  private CrystalBallNodeStates(CrystalBallTheme theme, CrystalBallEntry rootEntry, Map<CastPattern, Spell> spells,
      Set<CastPattern> explored) {
    this.theme = theme;
    this.rootEntry = rootEntry;
    this.spells = spells;
    this.explored = explored;
  }

  public static CrystalBallNodeStates forStaffType(Registry<Spell> registry, Holder<StaffType> staffType, Set<CastPattern> explored) {
    StaffType type = staffType.value();
    return new CrystalBallNodeStates(type.crystalBallTheme(), type.crystalBallEntry(),
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
  public Optional<Identifier> iconOf(CrystalBallNode node) {
    return entryOf(node).map(CrystalBallEntry::icon);
  }

  public Optional<CrystalBallEntry> entryOf(CrystalBallNode node) {
    if (node.isRoot()) {
      return Optional.of(rootEntry);
    }
    return spellAt(node).map(Spell::crystalBallEntry);
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
