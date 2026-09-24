package gragongit.arcaneartistry.common.spell;

import gragongit.arcaneartistry.common.api.CastProgressEvents;
import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.Staff;
import gragongit.arcaneartistry.common.staff.StaffDirection;
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
    if (player.level().isClientSide() || c.castPattern().isEmpty()) {
      return;
    }

    Staff staff = player.getUseItem().get(ModDataComponents.STAFF);
    if (staff == null) {
      return;
    }

    staff.type().value().strokeSound().ifPresent(sound -> playSound(player, sound.value(), getPitch(c.castPattern().getLast())));
  }

  public static void onCastProgressEnd(CastProgressContext c) {
    Player player = c.player();
    if (player.level().isClientSide() || c.castPattern().isEmpty()) {
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
        spell.castSound().ifPresent(sound -> playSound(player, sound.value(), 1F));
        spell.effect().onCast(player);
        return;
      }
    }

    staff.type().value().failSound().ifPresent(sound -> playSound(player, sound.value(), 1F));
  }

  private static void playSound(Player player, SoundEvent sound, float pitch) {
    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.7F, pitch);
  }

  private static float getPitch(StaffDirection direction) {
    switch (direction) {
      case StaffDirection.UP:
        return 1.5F;
      case StaffDirection.UP_LEFT:
        return 1.2F;
      case StaffDirection.UP_RIGHT:
        return 1.35F;
      case StaffDirection.LEFT:
        return 1.05F;
      case StaffDirection.RIGHT:
        return 0.95F;
      case StaffDirection.DOWN_LEFT:
        return 0.8F;
      case StaffDirection.DOWN_RIGHT:
        return 0.65F;
      case StaffDirection.DOWN:
        return 0.5F;
      default:
        return 1F;
    }
  }
}
