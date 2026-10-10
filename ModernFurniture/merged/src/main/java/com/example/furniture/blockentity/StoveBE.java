package com.example.furniture.blockentity;

import com.example.furniture.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StoveBE extends AbstractFurnaceBlockEntity {
    public StoveBE(BlockPos pos, BlockState state) { super(ModBlockEntities.STOVE, pos, state, RecipeType.SMELTING); }
    @Override protected Component getDefaultName() { return Component.translatable("container.modernfurniture.stove"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inv) { return new FurnaceMenu(id, inv, this, this.dataAccess); }
}
