package gragongit.arcaneartistry.common.block;

import gragongit.arcaneartistry.common.block.crystal.CrystalGeometry;
import gragongit.arcaneartistry.common.block.crystal.CrystalPlacement;
import gragongit.arcaneartistry.common.block.crystal.CrystalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A crystal that connects to neighbouring crystals of the same block. On sides facing air it is inset by {@link #INSET} pixels, towards
 * other crystals and full blocks it reaches the block boundary, and outer steps of a staircase are cut diagonally. The geometry is
 * described by {@link CrystalGeometry}.
 */
public class CrystalBlock extends Block {
  public static final int INSET = 2;

  public static final int EMPTY = 0;
  public static final int CRYSTAL = 1;
  public static final int SOLID = 2;

  public CrystalBlock(Properties properties) {
    super(properties);
  }

  /**
   * Classifies the block at {@code pos} relative to {@code crystal} as {@link #EMPTY}, {@link #CRYSTAL} or {@link #SOLID}.
   */
  public static int classify(BlockGetter level, BlockPos pos, Block crystal) {
    BlockState state = level.getBlockState(pos);
    if (state.is(crystal)) {
      return CRYSTAL;
    }
    return state.isCollisionShapeFullBlock(level, pos) ? SOLID : EMPTY;
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return CrystalShape.get(CrystalGeometry.shapeKey(level, pos, this));
  }

  /**
   * Places blocks against the side of a slope that is looked at most directly, see {@link CrystalPlacement}, instead of the side of the
   * step of the voxelized shape that happens to be hit.
   */
  @Override
  protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hit) {
    if (!(stack.getItem() instanceof BlockItem) || player.getCooldowns().isOnCooldown(stack)) {
      return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    Vec3 origin = Vec3.atLowerCornerOf(pos);
    Direction face = CrystalPlacement.face(CrystalGeometry.key(new CrystalGeometry.LevelCells(level, pos, this), true),
        player.getEyePosition().subtract(origin), hit.getLocation().subtract(origin), side -> level.getBlockState(pos.relative(side)).canBeReplaced());
    if (face == null || face == hit.getDirection()) {
      return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    int count = stack.getCount();
    InteractionResult result = stack.useOn(new UseOnContext(player, hand, hit.withDirection(face)));
    if (player.hasInfiniteMaterials()) {
      stack.setCount(count);
    }
    // Vanilla would otherwise retry with the side that was hit, placing the block somewhere unexpected.
    return result.consumesAction() ? result : InteractionResult.CONSUME;
  }

  @Override
  protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return Shapes.empty();
  }

  @Override
  protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
    return 1F;
  }

  @Override
  protected boolean propagatesSkylightDown(BlockState state) {
    return true;
  }
}
