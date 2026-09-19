package gragongit.arcaneartistry.common.spell;

import gragongit.arcaneartistry.common.api.CastProgressEvents;
import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.Staff;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SpellHandler {

  public static void init() {
    CastProgressEvents.STROKE_ADDED.register(SpellHandler::onCastProgressStrokeAdded);
    CastProgressEvents.STOP.register(SpellHandler::onCastProgressEnd);
  }

  public static void onCastProgressStrokeAdded(CastProgressContext c) {
    Player player = c.player();
    if (player.level().isClientSide()) {
      return;
    }

    Staff staff = player.getUseItem().get(ModDataComponents.STAFF);
    if (staff == null) {
      return;
    }

    staff.type().value().strokeSound().ifPresent(sound -> playSound(player, sound.value()));
  }

  public static void onCastProgressEnd(CastProgressContext c) {
    Player player = c.player();
    if (player.level().isClientSide()) {
      return;
    }

    Registry<Spell> spells = player.level().registryAccess().lookupOrThrow(ModRegistries.SPELL_KEY);
    ItemStack stack = player.getUseItem();
    Staff staff = stack.get(ModDataComponents.STAFF);
    if (staff == null) {
      return;
    }

    for (Spell spell : spells) {
      if (!staff.type().equals(spell.staffType())) {
        continue;
      }
      if (spell.pattern().equals(c.castPattern())) {
        spell.effect().onCast(player);
        return;
      }
    }

    staff.type().value().failSound().ifPresent(sound -> playSound(player, sound.value()));
  }

  private static void playSound(Player player, SoundEvent sound) {
    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
  }
}
