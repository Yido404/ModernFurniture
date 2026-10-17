package com.example.furniture;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ModTab {
    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, FurnitureMod.id("main"),
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.modernfurniture.main"))
                        .icon(() -> new ItemStack(ModBlocks.BLOCKS.get("sofa_red")))
                        .displayItems((params, out) -> ModBlocks.ITEMS.forEach(out::accept))
                        .build());
    }
}
