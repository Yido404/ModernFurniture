package com.example.furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Bunk bed = two real beds stacked. Placing the item creates the lower bed (2 blocks) AND the upper bed
 * (2 blocks above it), so both levels can be slept in. Breaking the lower bed removes the upper one too.
 */
public class BunkBed extends FurnitureBed {
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");

    public BunkBed(DyeColor color, BlockBehaviour.Properties props) {
        super(color, props);
        registerDefaultState(defaultBlockState().setValue(UPPER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UPPER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState s = super.getStateForPlacement(ctx);
        if (s == null) return null;
        Level level = ctx.getLevel();
        BlockPos foot = ctx.getClickedPos();
        BlockPos head = foot.relative(s.getValue(FACING));
        for (BlockPos p : new BlockPos[]{foot.above(), head.above()}) {
            if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced()) return null;
        }
        return s;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);   // places the lower head
        if (level instanceof ServerLevel) {
            BlockPos head = pos.relative(state.getValue(FACING));
            BlockState upper = state.setValue(UPPER, true);
            level.setBlock(pos.above(), upper, Block.UPDATE_ALL);
            level.setBlock(head.above(), upper.setValue(PART, BedPart.HEAD), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction dir, BlockPos npos, BlockState neighbor, RandomSource random) {
        if (state.getValue(UPPER) && dir == Direction.DOWN
                && !(neighbor.getBlock() == state.getBlock() && !neighbor.getValue(UPPER))) {
            return Blocks.AIR.defaultBlockState();   // lower bed is gone -> upper bed collapses
        }
        return super.updateShape(state, level, ticks, pos, dir, npos, neighbor, random);
    }
}
