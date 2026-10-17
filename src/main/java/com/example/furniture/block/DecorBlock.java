package com.example.furniture.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Plain, non-directional furniture (tables). */
public class DecorBlock extends Block {
    private final VoxelShape shape;
    public DecorBlock(BlockBehaviour.Properties props, VoxelShape shape) { super(props); this.shape = shape; }
    @Override protected MapCodec<? extends Block> codec() { throw new UnsupportedOperationException("not data-driven"); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return shape; }
}
