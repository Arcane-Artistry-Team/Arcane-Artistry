package gragongit.arcaneartistry.elements.common.spells.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.spell.SpellEffect;
import gragongit.arcaneartistry.common.spell.SpellEffectType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;

public record FireballEffect(int power) implements SpellEffect {
  public static final MapCodec<FireballEffect> CODEC = RecordCodecBuilder
      .mapCodec(
          instance -> instance.group(Codec.INT.fieldOf("power").forGetter(FireballEffect::power)).apply(instance, FireballEffect::new));

  @Override
  public SpellEffectType<?> type() {
    return SpellEffects.FIREBALL;
  }

  @Override
  public void onCast(Player player) {
    if (!(player.level() instanceof ServerLevel level)) {
      return;
    }

    LargeFireball fireball = new LargeFireball(level, player, player.getLookAngle(), power);
    fireball.setPos(player.getEyePosition().add(player.getLookAngle().scale(1.5)));
    level.addFreshEntity(fireball);
  }
}
