package gragongit.arcaneartistry.common.spell;

import net.minecraft.world.entity.player.Player;

public interface SpellEffect {
  SpellEffectType<?> type();

  void onCast(Player player);
}
