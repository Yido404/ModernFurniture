package com.example.furniture;

import com.example.furniture.entity.SeatEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static EntityType<SeatEntity> SEAT;

    public static void init() {
        Identifier id = FurnitureMod.id("seat");
        SEAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, id,
                EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                        .sized(0.001f, 0.001f).noSummon()
                        .clientTrackingRange(8).updateInterval(20)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }
}
