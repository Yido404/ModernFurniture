package com.example.furniture.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Multi-block crystal chandelier. Medium = 1x2x1, large = 3x2x3. The block you click is the top-centre
 * cell (the "ceiling" cell); the rest is built downwards / outwards. Right click toggles all cells.
 */
public class ChandelierBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final MultiPart.Dims MEDIUM = new MultiPart.Dims(1, 2, 1, 0, 1, 0);
    public static final MultiPart.Dims LARGE = new MultiPart.Dims(3, 2, 3, 1, 1, 1);
    private static final VoxelShape OUTLINE = Block.box(3, 0, 3, 13, 16, 13);
    private final MultiPart.Dims dims;

    public ChandelierBlock(BlockBehaviour.Properties props, MultiPart.Dims dims) {
        super(props);
        this.dims = dims;
        registerDefaultState(stateDefinition.any().setValue(LIT, false)
                .setValue(MultiPart.PX, dims.ix()).setValue(MultiPart.PY, dims.iy()).setValue(MultiPart.PZ, dims.iz()));
    }
    @Override protected MapCodec<? extends Block> codec() { throw new UnsupportedOperationException("not data-driven"); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(LIT, MultiPart.PX, MultiPart.PY, MultiPart.PZ);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        int x = state.getValue(MultiPart.PX), z = state.getValue(MultiPart.PZ);
        return switch (rotation) {
            case CLOCKWISE_90 -> state.setValue(MultiPart.PX, dims.sz() - 1 - z).setValue(MultiPart.PZ, x);
            case CLOCKWISE_180 -> state.setValue(MultiPart.PX, dims.sx() - 1 - x)
                    .setValue(MultiPart.PZ, dims.sz() - 1 - z);
            case COUNTERCLOCKWISE_90 -> state.setValue(MultiPart.PX, z)
                    .setValue(MultiPart.PZ, dims.sx() - 1 - x);
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case FRONT_BACK -> state.setValue(MultiPart.PX, dims.sx() - 1 - state.getValue(MultiPart.PX));
            case LEFT_RIGHT -> state.setValue(MultiPart.PZ, dims.sz() - 1 - state.getValue(MultiPart.PZ));
            default -> state;
        };
    }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return OUTLINE; }
    @Override protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return Shapes.empty(); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return MultiPart.canPlace(ctx.getLevel(), ctx.getClickedPos(), Direction.NORTH, dims) ? defaultBlockState() : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        MultiPart.forEach(pos, Direction.NORTH, dims, (p, x, y, z) -> {
            if (!p.equals(pos)) level.setBlock(p, state.setValue(MultiPart.PX, x).setValue(MultiPart.PY, y).setValue(MultiPart.PZ, z), Block.UPDATE_ALL);
        });
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction dir, BlockPos npos, BlockState neighbor, RandomSource random) {
        return MultiPart.shouldVanish(state, Direction.NORTH, dims, dir, neighbor) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel)) return InteractionResult.SUCCESS;
        BlockPos pivot = MultiPart.pivotOf(pos, Direction.NORTH, dims, state);
        boolean on = !state.getValue(LIT);
        MultiPart.forEach(pivot, Direction.NORTH, dims, (p, x, y, z) -> {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() == this) level.setBlock(p, s.setValue(LIT, on), Block.UPDATE_ALL);
        });
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8f, on ? 1.4f : 0.8f);
        return InteractionResult.SUCCESS;
    }
}
