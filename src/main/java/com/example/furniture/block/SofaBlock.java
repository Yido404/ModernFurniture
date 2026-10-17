package com.example.furniture.block;

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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SofaBlock extends SeatBlock {
    public static final EnumProperty<SofaShape> SHAPE = EnumProperty.create("shape", SofaShape.class);
    private static final VoxelShape NORTH = Shapes.or(Block.box(0, 0, 0, 16, 10, 16), Block.box(0, 10, 12, 16, 16, 16));

    public SofaBlock(BlockBehaviour.Properties props) {
        super(props, NORTH, 0.625, false);
        registerDefaultState(defaultBlockState().setValue(SHAPE, SofaShape.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHAPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction f = ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, f).setValue(SHAPE, computeShape(ctx.getLevel(), ctx.getClickedPos(), f));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction dir, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return state.setValue(SHAPE, computeShape(level, pos, state.getValue(FACING)));
    }

    /** Is the piece on the sitter's LEFT part of the same sofa run (straight sofa or a matching corner)? */
    private static boolean linkedLeft(BlockGetter level, BlockPos pos, Direction f) {
        BlockState s = level.getBlockState(pos.relative(f.getCounterClockWise()));
        if (s.getBlock() instanceof SofaBlock) return s.getValue(FACING) == f;
        if (s.getBlock() instanceof CornerSofaBlock) {
            Direction g = s.getValue(FACING);
            return s.getValue(CornerSofaBlock.MIRROR) ? g == f.getClockWise() : g == f;
        }
        return false;
    }

    private static boolean linkedRight(BlockGetter level, BlockPos pos, Direction f) {
        BlockState s = level.getBlockState(pos.relative(f.getClockWise()));
        if (s.getBlock() instanceof SofaBlock) return s.getValue(FACING) == f;
        if (s.getBlock() instanceof CornerSofaBlock) {
            Direction g = s.getValue(FACING);
            return s.getValue(CornerSofaBlock.MIRROR) ? g == f : g == f.getCounterClockWise();
        }
        return false;
    }

    private static SofaShape computeShape(BlockGetter level, BlockPos pos, Direction f) {
        boolean leftArm = !linkedLeft(level, pos, f);
        boolean rightArm = !linkedRight(level, pos, f);
        if (leftArm && rightArm) return SofaShape.SINGLE;
        if (leftArm) return SofaShape.LEFT;
        if (rightArm) return SofaShape.RIGHT;
        return SofaShape.MIDDLE;
    }
}
