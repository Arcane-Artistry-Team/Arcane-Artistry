package gragongit.arcaneartistry.client.renderer;

import gragongit.arcaneartistry.common.block.CrystalBlock;
import gragongit.arcaneartistry.common.block.crystal.CrystalGeometry;
import gragongit.arcaneartistry.common.block.crystal.CrystalOutline;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Draws the block outline of crystals along their actual geometry. Their {@link VoxelShape} is voxelized coarsely to keep collisions
 * cheap, so its outline would show steps instead of slopes.
 */
public final class CrystalOutlineRenderer {
  private CrystalOutlineRenderer() {}

  public static void register() {
    LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register((context, hit) -> {
      BlockOutlineRenderState state = context.levelState().blockOutlineRenderState;
      if (state == null) {
        return;
      }
      BlockState blockState = context.level().getBlockState(state.pos());
      if (!(blockState.getBlock() instanceof CrystalBlock)) {
        return;
      }
      CrystalGeometry.Key key = CrystalGeometry.key(new CrystalGeometry.LevelCells(context.level(), state.pos(), blockState.getBlock()), true);
      double[] edges = CrystalOutline.get(key);
      if (edges.length > 0) {
        context.levelState().blockOutlineRenderState = new BlockOutlineRenderState(state.pos(), state.isTranslucent(), state.highContrast(),
            new EdgeShape(edges), state.collisionShape(), state.occlusionShape(), state.interactionShape());
      }
    });
  }

  /**
   * A shape that is only used to draw the given edges as an outline. To anything else it looks like the bounding box of the edges.
   */
  private static final class EdgeShape extends VoxelShape {
    private final double[] edges;
    private final DoubleList[] coords = new DoubleList[3];

    EdgeShape(double[] edges) {
      super(BitSetDiscreteVoxelShape.withFilledBounds(1, 1, 1, 0, 0, 0, 1, 1, 1));
      this.edges = edges;
      for (int axis = 0; axis < 3; axis++) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = axis; i < edges.length; i += 3) {
          min = Math.min(min, edges[i]);
          max = Math.max(max, edges[i]);
        }
        coords[axis] = DoubleArrayList.wrap(new double[] {min, max});
      }
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
      return coords[axis.ordinal()];
    }

    @Override
    public void forAllEdges(Shapes.DoubleLineConsumer consumer) {
      for (int i = 0; i < edges.length; i += 6) {
        consumer.consume(edges[i], edges[i + 1], edges[i + 2], edges[i + 3], edges[i + 4], edges[i + 5]);
      }
    }
  }
}
