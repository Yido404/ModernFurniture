package com.example.furniture;

import com.example.furniture.blockentity.BigStorageBE;
import com.example.furniture.blockentity.FurnitureStorageBE;
import com.example.furniture.blockentity.OvenBE;
import com.example.furniture.blockentity.StoveBE;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static BlockEntityType<FurnitureStorageBE> STORAGE;
    public static BlockEntityType<BigStorageBE> BIG_STORAGE;
    public static BlockEntityType<StoveBE> STOVE;
    public static BlockEntityType<OvenBE> OVEN;

    private static <T extends BlockEntity> BlockEntityType<T> reg(String name, BlockEntityType<T> type) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, FurnitureMod.id(name), type);
    }

    public static void init() {
        STORAGE = reg("storage", FabricBlockEntityTypeBuilder.create(FurnitureStorageBE::new,
                ModBlocks.family("cabinet", "wardrobe", "fridge", "desk")).build());
        BIG_STORAGE = reg("big_storage", FabricBlockEntityTypeBuilder.create(BigStorageBE::new,
                ModBlocks.family("big_fridge", "big_wardrobe")).build());
        STOVE = reg("stove", FabricBlockEntityTypeBuilder.create(StoveBE::new, ModBlocks.family("stove")).build());
        OVEN = reg("oven", FabricBlockEntityTypeBuilder.create(OvenBE::new, ModBlocks.family("oven")).build());
    }
}
