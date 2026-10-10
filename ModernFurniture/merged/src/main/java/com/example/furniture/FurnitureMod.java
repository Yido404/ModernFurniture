package com.example.furniture;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class FurnitureMod implements ModInitializer {
    public static final String MOD_ID = "modernfurniture";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModTab.init();
    }
}
