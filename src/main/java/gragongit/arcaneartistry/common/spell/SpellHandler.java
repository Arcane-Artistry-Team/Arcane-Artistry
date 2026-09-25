package gragongit.arcaneartistry.common.spell;

import java.util.Optional;
import gragongit.arcaneartistry.common.api.CastProgressEvents;
import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.crystalball.CrystalBallState;
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

    ItemStack stack = player.getUseItem();
    Staff staff = stack.get(ModDataComponents.STAFF);
    if (staff == null) {
      return;
    }

    CrystalBallState.of(player).addExplored(staff.type().unwrapKey().orElseThrow(), c.castPattern());

    Registry<Spell> spells = player.level().registryAccess().lookupOrThrow(ModRegistries.SPELL_KEY);
    Optional<Spell> spell = SpellsByStaffType.of(spells).find(staff.type(), c.castPattern());
    if (spell.isPresent()) {
      spell.get().castSound().ifPresent(sound -> playSound(player, sound.value(), 1F));
      spell.get().effect().onCast(player);
    } else {
      staff.type().value().failSound().ifPresent(sound -> playSound(player, sound.value(), 1F));
    }
  }

  private static void playSound(Player player, SoundEvent sound, float pitch) {
    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.7F, pitch);
  }

  private static float getPitch(StaffDirection direction) {
    switch (direction) {
      case StaffDirection.UP:
        return 1.2F;
      case StaffDirection.LEFT:
        return 1.05F;
      case StaffDirection.RIGHT:
        return 0.95F;
      case StaffDirection.DOWN:
        return 0.8F;
      default:
        return 1F;
    }
  }
}
