package com.example.furniture.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Corner piece of an L-shaped sofa: backrests behind and on one side.
 * Normal placement = backrest on the left; place while sneaking = mirrored (backrest on the right).
 * Adjacent SofaBlocks automatically drop the armrest on the side that touches the corner.
 */
public class CornerSofaBlock extends SeatBlock {
    public static final BooleanProperty MIRROR = BooleanProperty.create("mirror");
    private static final VoxelShape NORTH = Shapes.or(Block.box(0, 0, 0, 16, 10, 16), Block.box(0, 10, 12, 16, 16, 16));

    public CornerSofaBlock(BlockBehaviour.Properties props) {
        super(props, NORTH, 0.625, false);
        registerDefaultState(defaultBlockState().setValue(MIRROR, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MIRROR);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        boolean mirror = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown();
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(MIRROR, mirror);
    }
}
