package com.example.furniture.blockentity;

import com.example.furniture.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class OvenBE extends AbstractFurnaceBlockEntity {
    public OvenBE(BlockPos pos, BlockState state) { super(ModBlockEntities.OVEN, pos, state, RecipeType.SMOKING); }
    @Override protected Component getDefaultName() { return Component.translatable("container.modernfurniture.oven"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inv) { return new SmokerMenu(id, inv, this, this.dataAccess); }
}
