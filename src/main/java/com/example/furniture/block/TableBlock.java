package com.example.furniture.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Dining table that merges with neighbouring tables into one long/large table; legs only stay on the outer corners. */
public class TableBlock extends Block {
    public static final BooleanProperty N = BlockStateProperties.NORTH, E = BlockStateProperties.EAST,
            S = BlockStateProperties.SOUTH, W = BlockStateProperties.WEST;
    private static final VoxelShape TOP = Block.box(0, 13, 0, 16, 16, 16);
    private static final VoxelShape NW = Block.box(1, 0, 1, 3, 13, 3), NE = Block.box(13, 0, 1, 15, 13, 3),
            SW = Block.box(1, 0, 13, 3, 13, 15), SE = Block.box(13, 0, 13, 15, 13, 15);

    public TableBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(N, false).setValue(E, false).setValue(S, false).setValue(W, false));
    }
    @Override protected MapCodec<? extends Block> codec() { throw new UnsupportedOperationException("not data-driven"); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(N, E, S, W); }

    private static boolean joins(BlockGetter l, BlockPos p) { return l.getBlockState(p).getBlock() instanceof TableBlock; }

    private BlockState connect(BlockState s, BlockGetter l, BlockPos p) {
        return s.setValue(N, joins(l, p.north())).setValue(E, joins(l, p.east()))
                .setValue(S, joins(l, p.south())).setValue(W, joins(l, p.west()));
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return connect(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction dir, BlockPos npos, BlockState neighbor, RandomSource random) {
        return connect(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        VoxelShape shape = TOP;
        if (!s.getValue(N) && !s.getValue(W)) shape = Shapes.or(shape, NW);
        if (!s.getValue(N) && !s.getValue(E)) shape = Shapes.or(shape, NE);
        if (!s.getValue(S) && !s.getValue(W)) shape = Shapes.or(shape, SW);
        if (!s.getValue(S) && !s.getValue(E)) shape = Shapes.or(shape, SE);
        return shape;
    }
}
