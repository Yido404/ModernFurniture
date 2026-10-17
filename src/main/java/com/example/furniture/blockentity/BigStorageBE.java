package com.example.furniture.blockentity;

import com.example.furniture.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BigStorageBE extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
    public BigStorageBE(BlockPos pos, BlockState state) { super(ModBlockEntities.BIG_STORAGE, pos, state); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> list) { this.items = list; }
    @Override protected Component getDefaultName() { return Component.translatable("container.modernfurniture.big_storage"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inv) { return ChestMenu.sixRows(id, inv, this); }
    @Override public int getContainerSize() { return 54; }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(in)) ContainerHelper.loadAllItems(in, items);
    }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (!trySaveLootTable(out)) ContainerHelper.saveAllItems(out, items);
    }
}
