package gragongit.arcaneartistry.common.spell;

import gragongit.arcaneartistry.common.api.CastProgressEvents.CastProgressContext;
import gragongit.arcaneartistry.common.registry.ModDataComponents;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.Staff;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public final class SpellHandler {
  public static void onCastProgressEnd(CastProgressContext c) {
    if (c.player().level().isClientSide()) {
      return;
    }
    Registry<Spell> spells = c.player().level().registryAccess().lookupOrThrow(ModRegistries.SPELL_KEY);
    ItemStack stack = c.player().getUseItem();
    Staff staff = stack.get(ModDataComponents.STAFF);
    if (staff == null) {
      return;
    }

    for (Spell spell : spells) {
      if (staff.type() != spell.staffType()) {
        continue;
      }
      if (spell.pattern().equals(c.castPattern())) {
        spell.effect().onCast(c.player());
        break;
      }
    }
  }
}
