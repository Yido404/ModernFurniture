package com.example.furniture.block;

import com.example.furniture.ModEntities;
import com.example.furniture.entity.SeatEntity;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Chair-like furniture: right click to sit, sneak / jump-off to stand up. */
public class SeatBlock extends HorizontalDirectionalBlock {
    private final Map<Direction, VoxelShape> shapes;
    private final double seatTop;   // height (in blocks) of the seat surface
    private final boolean heals;    // recliner: slowly heals the sitter

    public SeatBlock(BlockBehaviour.Properties props, VoxelShape north, double seatTop, boolean heals) {
        super(props);
        this.shapes = ShapeUtil.rotations(north);
        this.seatTop = seatTop;
        this.heals = heals;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        throw new UnsupportedOperationException("not data-driven");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (player.isPassenger() || player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (!level.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty()) return InteractionResult.CONSUME;

        Direction facing = state.getValue(FACING);
        SeatEntity seat = new SeatEntity(ModEntities.SEAT, level);
        // the player's "vehicle attachment" is at hip height, so the seat entity sits exactly on the cushion
        seat.setPos(pos.getX() + 0.5, pos.getY() + seatTop, pos.getZ() + 0.5);
        seat.setYRot(facing.toYRot());
        seat.setHeals(heals);
        server.addFreshEntity(seat);
        player.setYRot(facing.toYRot());
        if (!player.startRiding(seat)) {
            seat.discard();   // e.g. vanilla's 3 second cooldown right after standing up
            player.displayClientMessage(Component.translatable("message.modernfurniture.cannot_sit"), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }
}
