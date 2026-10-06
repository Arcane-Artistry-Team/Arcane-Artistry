package gragongit.arcaneartistry.common.block;

import org.jspecify.annotations.Nullable;
import gragongit.arcaneartistry.common.ArcaneArtistryConfig;
import gragongit.arcaneartistry.common.crystalball.CrystalBallPayload;
import gragongit.arcaneartistry.common.registry.ModBlockEntities;
import gragongit.arcaneartistry.common.registry.ModItems;
import gragongit.arcaneartistry.common.staff.Staff;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OrbRingBlock extends Block implements EntityBlock {
  public static final BooleanProperty CRYSTAL = BooleanProperty.create("crystal");

  private static final VoxelShape SHAPE = Shapes.or(Block.box(3, 0, 3, 13, 1, 13), Block.box(4, 1, 4, 12, 4, 12));
  private static final VoxelShape SHAPE_WITH_CRYSTAL = Shapes.or(SHAPE, Block.box(4, 3, 4, 12, 11, 12));

  public OrbRingBlock(Properties properties) {
    super(properties);
    registerDefaultState(defaultBlockState().setValue(CRYSTAL, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(CRYSTAL);
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return state.getValue(CRYSTAL) ? SHAPE_WITH_CRYSTAL : SHAPE;
  }

  @Override
  protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    BlockPos below = pos.below();
    return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
  }

  @Override
  protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
      Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
    return directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
        : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
  }

  @Override
  protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    if (!state.getValue(CRYSTAL)) {
      if (!stack.is(ModItems.LAPIS_CRYSTAL)) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
      }

      if (!level.isClientSide()) {
        level.setBlock(pos, state.setValue(CRYSTAL, true), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS, 1F, 1F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        stack.consume(1, player);
      }
      return InteractionResult.SUCCESS;
    }

    if (stack.is(Items.BOOK)) {
      if (level instanceof ServerLevel serverLevel) {
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(ModItems.GUIDE_BOOK)));
        serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1F, 1.2F);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 40, 0.3, 0.3, 0.3, 0.5);
        level.gameEvent(player, GameEvent.ITEM_INTERACT_FINISH, pos);
      }
      return InteractionResult.SUCCESS;
    }

    Staff staff = Staff.get(stack);
    if (staff == null) {
      return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    if (player instanceof ServerPlayer serverPlayer) {
      level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1F, 1F);
      ServerPlayNetworking.send(serverPlayer, new CrystalBallPayload(staff.type(), ArcaneArtistryConfig.maxPatternLength(), pos));
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!state.getValue(CRYSTAL) || Staff.is(player.getOffhandItem())) {
      return InteractionResult.PASS;
    }

    if (!level.isClientSide()) {
      level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1F, 1F);
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new OrbRingBlockEntity(pos, state);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return level.isClientSide() && type == ModBlockEntities.ORB_RING
        ? (BlockEntityTicker<T>) (BlockEntityTicker<OrbRingBlockEntity>) OrbRingBlockEntity::clientTick
        : null;
  }
}
