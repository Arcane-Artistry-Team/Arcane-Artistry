package gragongit.arcaneartistry.elements.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.spell.SpellEffect;
import gragongit.arcaneartistry.common.spell.SpellEffectType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public record FireballEffect(int damage) implements SpellEffect {
  public static final MapCodec<FireballEffect> CODEC = RecordCodecBuilder
      .mapCodec(
          instance -> instance.group(Codec.INT.fieldOf("damage").forGetter(FireballEffect::damage)).apply(instance, FireballEffect::new));

  @Override
  public SpellEffectType<?> type() {
    return ArcaneArtistryElements.FIREBALL;
  }

  @Override
  public void onCast(Player player) {
    player.sendOverlayMessage(Component.literal("FIREBALL!"));
  }
}
