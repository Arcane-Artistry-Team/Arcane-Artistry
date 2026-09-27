package gragongit.arcaneartistry.common.mana;

import net.minecraft.world.entity.player.Player;

public final class ManaState {
  public static final float TICKS_PER_SECOND = 20F;

  public static final float BASE_REGEN_PER_SECOND = 5F / 3F;
  public static final float REGEN_ACCELERATION_PER_SECOND_SQ = 2F / 3F;

  private final Player player;

  private ManaState(Player player) {
    this.player = player;
  }

  public static ManaState of(Player player) {
    return new ManaState(player);
  }

  public float getMax() {
    return (float) player.getAttributeValue(ManaAttributes.MAX_MANA);
  }

  public float getMana() {
    return Math.min(player.getAttachedOrElse(ManaAttachments.MANA, getMax()), getMax());
  }

  public boolean tryConsume(int cost) {
    float mana = getMana();
    if (mana < cost) {
      return false;
    }
    player.setAttached(ManaAttachments.MANA, mana - cost);
    player.setAttached(ManaAttachments.TICKS_SINCE_CAST, 0);
    return true;
  }

  public void tick() {
    float max = getMax();
    float stored = player.getAttachedOrElse(ManaAttachments.MANA, max);
    float mana = Math.min(stored, max);

    if (mana < max) {
      int ticksSinceCast = player.getAttachedOrElse(ManaAttachments.TICKS_SINCE_CAST, 0) + 1;
      player.setAttached(ManaAttachments.TICKS_SINCE_CAST, ticksSinceCast);
      mana = Math.min(mana + regenPerTick(ticksSinceCast), max);
    }

    if (mana != stored) {
      player.setAttached(ManaAttachments.MANA, mana);
    }
  }

  private static float regenPerTick(int ticksSinceCast) {
    float secondsSinceCast = ticksSinceCast / TICKS_PER_SECOND;
    return (BASE_REGEN_PER_SECOND + REGEN_ACCELERATION_PER_SECOND_SQ * secondsSinceCast) / TICKS_PER_SECOND;
  }
}
