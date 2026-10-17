package com.example.furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A real vanilla BedBlock (sleep, skip the night, set spawn, explode in other dimensions...)
 * rendered with a normal JSON model instead of the hard-coded bed block-entity renderer.
 */
public class FurnitureBed extends BedBlock {
    public FurnitureBed(DyeColor color, BlockBehaviour.Properties props) { super(color, props); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return null; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
