package com.example.furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Bed that merges sideways with identical beds: place two next to each other for a double/king bed. Every cell is sleepable. */
public class DoubleBed extends FurnitureBed {
    public static final EnumProperty<SofaShape> SIDE = EnumProperty.create("side", SofaShape.class);

    public DoubleBed(DyeColor color, BlockBehaviour.Properties props) {
        super(color, props);
        registerDefaultState(defaultBlockState().setValue(SIDE, SofaShape.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SIDE);
    }

    private static boolean same(BlockGetter level, BlockPos pos, Direction f, BedPart part) {
        BlockState s = level.getBlockState(pos);
        return s.getBlock() instanceof DoubleBed && s.getValue(FACING) == f && s.getValue(PART) == part;
    }

    private static SofaShape side(BlockGetter level, BlockPos pos, BlockState s) {
        Direction f = s.getValue(FACING);
        BedPart part = s.getValue(PART);
        boolean leftEdge = !same(level, pos.relative(f.getCounterClockWise()), f, part);
        boolean rightEdge = !same(level, pos.relative(f.getClockWise()), f, part);
        if (leftEdge && rightEdge) return SofaShape.SINGLE;
        if (leftEdge) return SofaShape.LEFT;
        if (rightEdge) return SofaShape.RIGHT;
        return SofaShape.MIDDLE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState s = super.getStateForPlacement(ctx);
        return s == null ? null : s.setValue(SIDE, side(ctx.getLevel(), ctx.getClickedPos(), s));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel) {
            BlockPos head = pos.relative(state.getValue(FACING));
            BlockState hs = level.getBlockState(head);
            if (hs.getBlock() == this) level.setBlock(head, hs.setValue(SIDE, side(level, head, hs)), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction dir, BlockPos npos, BlockState neighbor, RandomSource random) {
        BlockState result = super.updateShape(state, level, ticks, pos, dir, npos, neighbor, random);
        if (result.getBlock() != this) return result;   // became air (other half missing)
        return result.setValue(SIDE, side(level, pos, result));
    }
}
