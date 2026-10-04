package gragongit.arcaneartistry.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OrbRingBlock extends Block {
  private static final VoxelShape SHAPE = Shapes.or(Block.box(3, 0, 3, 13, 1, 13), Block.box(5, 1, 5, 11, 4, 11));

  public OrbRingBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }
}
