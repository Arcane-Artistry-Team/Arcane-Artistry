package gragongit.arcaneartistry.elements.common.staffs;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import gragongit.arcaneartistry.common.api.CastProgressEvents;
import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.staff.Staff;
import gragongit.arcaneartistry.common.staff.StaffDirection;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class CastingEffects {
  private static final Map<StaffType, SoundEvent> CASTING_STROKE_SOUNDS = new IdentityHashMap<>();
  private static final Map<StaffType, SoundEvent> CASTING_CAST_SOUNDS = new IdentityHashMap<>();
  private static final Map<StaffDirection, Float> DIRECTION_PITCHES = new EnumMap<>(StaffDirection.class);

  static {
    CASTING_STROKE_SOUNDS.put(StaffTypes.FIRE, SoundEvents.FIRECHARGE_USE);

    CASTING_CAST_SOUNDS.put(StaffTypes.FIRE, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST);

    DIRECTION_PITCHES.put(StaffDirection.UP, 1.2f);
    DIRECTION_PITCHES.put(StaffDirection.LEFT, 1.05f);
    DIRECTION_PITCHES.put(StaffDirection.RIGHT, 0.95f);
    DIRECTION_PITCHES.put(StaffDirection.DOWN, 0.8f);
  }

  private CastingEffects() {}

  public static void init() {
    CastProgressEvents.STROKE_ADDED.register(CastingEffects::onStrokeAdded);
    CastProgressEvents.STOP.register(CastingEffects::onCastProgressStop);
  }

  private static void onStrokeAdded(CastProgressContext c) {
    float pitch = DIRECTION_PITCHES.getOrDefault(c.castPattern().strokes().getLast(), 1.0f);
    playCastingSound(c, CASTING_STROKE_SOUNDS, pitch);
  }

  private static void onCastProgressStop(CastProgressContext c) {
    Registry<Spell> spells = c.player().level().registryAccess().lookupOrThrow(ModRegistries.SPELL_KEY);
    Staff staff = Staff.get(c.player());
    if (staff == null) {
      return;
    }

    for (Spell spell : spells) {
      if (staff.type() != spell.staffType()) {
        continue;
      }
      if (spell.pattern().equals(c.castPattern())) {
        spell.effect().onCast(c.player());
        playCastingSound(c, CASTING_CAST_SOUNDS, 1.0f);
        break;
      }
    }
  }

  private static void playCastingSound(CastProgressContext c, Map<StaffType, SoundEvent> sounds, float pitch) {
    if (c.player().level().isClientSide()) {
      return;
    }

    Staff staff = Staff.get(c.player());
    if (staff == null) {
      return;
    }

    SoundEvent sound = sounds.get(staff.type());
    if (sound == null) {
      return;
    }

    c.player().level().playSound(null, c.player().blockPosition(), sound, SoundSource.PLAYERS, 1.0f, pitch);
  }
}
