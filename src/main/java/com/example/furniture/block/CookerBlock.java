package com.example.furniture.block;

import com.example.furniture.ModBlockEntities;
import com.example.furniture.blockentity.OvenBE;
import com.example.furniture.blockentity.StoveBE;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stove = furnace (smelts / cooks anything a furnace can, burns fuel).
 * Oven  = smoker-style (bakes and cooks food only, but faster).
 */
public class CookerBlock extends AbstractFurnaceBlock {
    private final boolean oven;

    public CookerBlock(BlockBehaviour.Properties props, boolean oven) {
        super(props);
        this.oven = oven;
    }
    @Override protected MapCodec<? extends AbstractFurnaceBlock> codec() { throw new UnsupportedOperationException("not data-driven"); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return oven ? new OvenBE(pos, state) : new StoveBE(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, oven ? ModBlockEntities.OVEN : ModBlockEntities.STOVE);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof MenuProvider provider) player.openMenu(provider);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 0, 0.03, 0);
            if (!oven) level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.0,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.0, 0);
        }
    }
}
