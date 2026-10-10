package com.example.furniture.block;

import com.example.furniture.blockentity.BigStorageBE;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/** 2 wide x 2 tall double-door storage (big fridge / big wardrobe). 54 slots. Pivot = bottom-left cell. */
public class MultiStorageBlock extends BaseEntityBlock {
    public static final MultiPart.Dims DIMS = new MultiPart.Dims(2, 2, 1, 0, 0, 0);

    public MultiStorageBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(MultiPart.PX, 0).setValue(MultiPart.PY, 0).setValue(MultiPart.PZ, 0)
                .setValue(MultiPart.MIRRORED, false));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { throw new UnsupportedOperationException("not data-driven"); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(HorizontalDirectionalBlock.FACING, MultiPart.PX, MultiPart.PY, MultiPart.PZ, MultiPart.MIRRORED);
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HorizontalDirectionalBlock.FACING,
                rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        // Keep PX=0 on the original block-entity cell, even when reflection swaps its world side.
        return state.setValue(HorizontalDirectionalBlock.FACING,
                        mirror.mirror(state.getValue(HorizontalDirectionalBlock.FACING)))
                .setValue(MultiPart.MIRRORED, !state.getValue(MultiPart.MIRRORED));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction f = ctx.getHorizontalDirection().getOpposite();
        if (!MultiPart.canPlace(ctx.getLevel(), ctx.getClickedPos(), f, DIMS)) return null;
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, f);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        Direction f = state.getValue(HorizontalDirectionalBlock.FACING);
        MultiPart.forEach(pos, f, DIMS, (p, x, y, z) -> {
            if (!p.equals(pos)) level.setBlock(p, state.setValue(MultiPart.PX, x).setValue(MultiPart.PY, y).setValue(MultiPart.PZ, z), Block.UPDATE_ALL);
        });
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction dir, BlockPos npos, BlockState neighbor, RandomSource random) {
        if (MultiPart.shouldVanish(state, state.getValue(HorizontalDirectionalBlock.FACING), DIMS, dir,
                neighbor, state.getValue(MultiPart.MIRRORED)))
            return Blocks.AIR.defaultBlockState();
        return state;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(MultiPart.PX) == 0 && state.getValue(MultiPart.PY) == 0 ? new BigStorageBE(pos, state) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel)) return InteractionResult.SUCCESS;
        BlockPos pivot = MultiPart.pivotOf(pos, state.getValue(HorizontalDirectionalBlock.FACING), DIMS,
                state, state.getValue(MultiPart.MIRRORED));
        if (level.getBlockEntity(pivot) instanceof MenuProvider provider) {
            player.openMenu(provider);
            level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.6f, 0.9f);
        }
        return InteractionResult.CONSUME;
    }
}
