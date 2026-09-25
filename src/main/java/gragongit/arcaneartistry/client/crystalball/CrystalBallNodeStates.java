package gragongit.arcaneartistry.client.crystalball;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.spell.SpellsByStaffType;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

public final class CrystalBallNodeStates implements CrystalBallRenderer.ChrystalBallNodeStateProvider {
  private final Map<CastPattern, Spell> spells;
  private final Set<CastPattern> explored;
  private final Map<CrystalBallNode, CrystalBallNodeState> cache = new IdentityHashMap<>();

  private CrystalBallNodeStates(Map<CastPattern, Spell> spells, Set<CastPattern> explored) {
    this.spells = spells;
    this.explored = explored;
  }

  public static CrystalBallNodeStates forStaffType(Registry<Spell> registry, Holder<StaffType> staffType, Set<CastPattern> explored) {
    return new CrystalBallNodeStates(SpellsByStaffType.of(registry).forStaffType(staffType), explored);
  }

  @Override
  public CrystalBallNodeState stateOf(CrystalBallNode node) {
    return cache.computeIfAbsent(node, this::compute);
  }

  private CrystalBallNodeState compute(CrystalBallNode node) {
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
