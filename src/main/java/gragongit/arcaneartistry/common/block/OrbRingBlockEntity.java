package gragongit.arcaneartistry.common.block;

import gragongit.arcaneartistry.common.registry.ModBlockEntities;
import gragongit.arcaneartistry.common.staff.Staff;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OrbRingBlockEntity extends BlockEntity {
  private static final double ACTIVATION_RANGE = 7.0;
  private static final float ACTIVATION_TICKS = 20F;
  private static final float ACTIVATION_SOUND_VOLUME = 0.6F;
  private static final float ACTIVATION_SOUND_PITCH = 1.3F;

  private static final float SPIN_SPEED = Mth.TWO_PI / 600F;
  private static final float SPIN_ACCELERATION = SPIN_SPEED / 20F;
  private static final float RESTING_ANGLE_STEP = Mth.HALF_PI;
  private static final float SETTLE_DESCENT_FRACTION = 0.7F;

  private boolean active;
  private float activation;
  private float previousActivation;
  private float spinAngle;
  private float previousSpinAngle;
  private float spinVelocity;

  private boolean settling;
  private int settleTick;
  private int settleDuration;
  private float settleStartAngle;
  private float settleStartVelocity;
  private float settleTargetAngle;

  public OrbRingBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.ORB_RING, pos, state);
  }

  public static void clientTick(Level level, BlockPos pos, BlockState state, OrbRingBlockEntity orbRing) {
    boolean active = state.getValue(OrbRingBlock.CRYSTAL) && isStaffNearby(level, pos);
    if (active != orbRing.active) {
      orbRing.active = active;
      level
          .playLocalSound(pos, active ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, ACTIVATION_SOUND_VOLUME,
              ACTIVATION_SOUND_PITCH, false);
    }

    orbRing.previousActivation = orbRing.activation;
    orbRing.activation = Mth.approach(orbRing.activation, active ? 1F : 0F, 1F / ACTIVATION_TICKS);
    orbRing.tickSpin(active);
  }

  private void tickSpin(boolean active) {
    previousSpinAngle = spinAngle;

    if (active) {
      settling = false;
      spinVelocity = Mth.approach(spinVelocity, SPIN_SPEED, SPIN_ACCELERATION);
      spinAngle += spinVelocity;
    } else {
      if (!settling && (spinVelocity != 0 || spinAngle != settleTargetAngle)) {
        startSettling();
      }
      if (settling) {
        tickSettling();
      }
    }

    if (spinAngle >= Mth.TWO_PI) {
      spinAngle -= Mth.TWO_PI;
      previousSpinAngle -= Mth.TWO_PI;
      settleStartAngle -= Mth.TWO_PI;
      settleTargetAngle -= Mth.TWO_PI;
    }
  }

  private void startSettling() {
    settling = true;
    settleTick = 0;
    settleDuration = Math.max(1, Mth.ceil(previousActivation * ACTIVATION_TICKS * SETTLE_DESCENT_FRACTION));
    settleStartAngle = spinAngle;
    settleStartVelocity = spinVelocity;
    settleTargetAngle = Math.round(spinAngle / RESTING_ANGLE_STEP) * RESTING_ANGLE_STEP;
  }

  private void tickSettling() {
    settleTick++;
    if (settleTick >= settleDuration) {
      settling = false;
      spinAngle = settleTargetAngle;
      spinVelocity = 0;
      return;
    }

    float t = (float) settleTick / settleDuration;
    float t2 = t * t;
    float t3 = t2 * t;
    spinAngle = (2 * t3 - 3 * t2 + 1) * settleStartAngle + (t3 - 2 * t2 + t) * settleStartVelocity * settleDuration
        + (-2 * t3 + 3 * t2) * settleTargetAngle;
    spinVelocity = spinAngle - previousSpinAngle;
  }

  private static boolean isStaffNearby(Level level, BlockPos pos) {
    Vec3 center = Vec3.atCenterOf(pos);
    for (Player player : level.players()) {
      if (!player.isSpectator() && player.distanceToSqr(center) <= ACTIVATION_RANGE * ACTIVATION_RANGE
          && (Staff.is(player.getMainHandItem()) || Staff.is(player.getOffhandItem()))) {
        return true;
      }
    }
    return false;
  }

  public float getActivation(float partialTicks) {
    return Mth.lerp(partialTicks, previousActivation, activation);
  }

  public float getSpinAngle(float partialTicks) {
    return Mth.lerp(partialTicks, previousSpinAngle, spinAngle);
  }
}
