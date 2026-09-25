package gragongit.arcaneartistry.common.spell;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.staff.StaffType;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

public final class SpellsByStaffType {
  private static final Map<Registry<Spell>, SpellsByStaffType> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

  private final Reference2ObjectMap<Holder<StaffType>, Object2ObjectMap<CastPattern, Spell>> byStaffType;

  private SpellsByStaffType(Reference2ObjectMap<Holder<StaffType>, Object2ObjectMap<CastPattern, Spell>> byStaffType) {
    this.byStaffType = byStaffType;
  }

  public static SpellsByStaffType of(Registry<Spell> spells) {
    return CACHE.computeIfAbsent(spells, SpellsByStaffType::compute);
  }

  private static SpellsByStaffType compute(Registry<Spell> spells) {
    Reference2ObjectMap<Holder<StaffType>, Object2ObjectMap<CastPattern, Spell>> grouped = new Reference2ObjectOpenHashMap<>();
    for (Spell spell : spells) {
      grouped.computeIfAbsent(spell.staffType(), type -> new Object2ObjectOpenHashMap<>()).put(spell.pattern(), spell);
    }
    grouped.replaceAll((type, patterns) -> Object2ObjectMaps.unmodifiable(patterns));
    return new SpellsByStaffType(Reference2ObjectMaps.unmodifiable(grouped));
  }

  public Map<CastPattern, Spell> forStaffType(Holder<StaffType> staffType) {
    return byStaffType.getOrDefault(staffType, Object2ObjectMaps.emptyMap());
  }

  public Optional<Spell> find(Holder<StaffType> staffType, CastPattern pattern) {
    return Optional.ofNullable(forStaffType(staffType).get(pattern));
  }
}
