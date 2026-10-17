package com.example.furniture;

import com.example.furniture.block.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Registers every furniture family in all 16 dye colours (family_color). */
public final class ModBlocks {
    public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, List<Block>> FAMILIES = new LinkedHashMap<>();
    public static final List<Item> ITEMS = new ArrayList<>();

    private static VoxelShape b(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    // shapes are written for "facing north" and rotated automatically
    private static final VoxelShape SEAT = Shapes.or(b(0, 0, 0, 16, 10, 16), b(0, 10, 12, 16, 16, 16));
    private static final VoxelShape RECLINER = Shapes.or(b(0, 0, 4, 16, 10, 16), b(0, 0, 0, 16, 7, 4), b(0, 10, 12, 16, 15, 16));
    private static final VoxelShape CHAIR = Shapes.or(b(1, 0, 1, 15, 10, 15), b(1, 10, 13, 15, 16, 15));
    private static final VoxelShape STOOL = b(3, 0, 3, 13, 11, 13);
    private static final VoxelShape TABLE = Shapes.or(b(0, 13, 0, 16, 16, 16), b(1, 0, 1, 3, 13, 3), b(13, 0, 1, 15, 13, 3),
            b(1, 0, 13, 3, 13, 15), b(13, 0, 13, 15, 13, 15));
    private static final VoxelShape COFFEE = Shapes.or(b(0, 7, 0, 16, 9, 16), b(1, 0, 1, 3, 7, 3), b(13, 0, 1, 15, 7, 3),
            b(1, 0, 13, 3, 7, 15), b(13, 0, 13, 15, 7, 15));

    private static final VoxelShape DESK = Shapes.or(b(0, 13, 0, 16, 16, 16), b(9, 0, 1, 16, 13, 15), b(1, 0, 1, 3, 13, 3), b(1, 0, 13, 3, 13, 15));
    private static final VoxelShape SIDE_TABLE = Shapes.or(b(3, 10, 3, 13, 12, 13), b(7, 0, 7, 9, 10, 9), b(4, 0, 4, 12, 1, 12));
    private static final VoxelShape BAR_TABLE = Shapes.or(b(0, 15, 0, 16, 18, 16), b(7, 0, 7, 9, 15, 9), b(3, 0, 3, 13, 1, 13));

    private static BlockBehaviour.Properties base(DyeColor c, SoundType s, float hardness) {
        return BlockBehaviour.Properties.of().mapColor(c).sound(s).strength(hardness).noOcclusion();
    }
    private static BlockBehaviour.Properties soft(DyeColor c) { return base(c, SoundType.WOOL, 0.8f); }
    private static BlockBehaviour.Properties wood(DyeColor c) { return base(c, SoundType.WOOD, 1.5f); }
    private static BlockBehaviour.Properties metal(DyeColor c) { return base(c, SoundType.METAL, 3.0f); }
    private static BlockBehaviour.Properties lamp(DyeColor c) {
        return base(c, SoundType.WOOL, 0.5f).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 15 : 0);
    }
    private static BlockBehaviour.Properties cooker(DyeColor c) {
        return base(c, SoundType.METAL, 3.0f).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0);
    }
    private static BlockBehaviour.Properties bed(DyeColor c) {
        return BlockBehaviour.Properties.of().mapColor(c).sound(SoundType.WOOD).strength(0.2f).noOcclusion().ignitedByLava();
    }

    private static void family(String name, Function<DyeColor, BlockBehaviour.Properties> props,
                               BiFunction<DyeColor, BlockBehaviour.Properties, ? extends Block> maker) {
        List<Block> list = new ArrayList<>();
        for (DyeColor c : DyeColor.values()) {
            String path = name + "_" + c.getName();
            Identifier id = FurnitureMod.id(path);
            Block block = maker.apply(c, props.apply(c).setId(ResourceKey.create(Registries.BLOCK, id)));
            Registry.register(BuiltInRegistries.BLOCK, id, block);
            Item item = new BlockItem(block, new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix());
            Registry.register(BuiltInRegistries.ITEM, id, item);
            BLOCKS.put(path, block);
            ITEMS.add(item);
            list.add(block);
        }
        FAMILIES.put(name, list);
    }

    public static Block[] family(String... names) {
        List<Block> out = new ArrayList<>();
        for (String n : names) out.addAll(FAMILIES.get(n));
        return out.toArray(new Block[0]);
    }

    public static void init() {
        // --- seating ---
        family("sofa", ModBlocks::soft, (c, p) -> new SofaBlock(p));
        family("corner_sofa", ModBlocks::soft, (c, p) -> new CornerSofaBlock(p));
        family("armchair", ModBlocks::soft, (c, p) -> new SeatBlock(p, SEAT, 0.625, false));
        family("recliner", ModBlocks::soft, (c, p) -> new SeatBlock(p, RECLINER, 0.625, true));
        family("dining_chair", ModBlocks::wood, (c, p) -> new SeatBlock(p, CHAIR, 0.625, false));
        family("stool", ModBlocks::wood, (c, p) -> new SeatBlock(p, STOOL, 0.6875, false));
        // --- tables & storage ---
        family("table", ModBlocks::wood, (c, p) -> new TableBlock(p));
        family("side_table", ModBlocks::wood, (c, p) -> new DecorBlock(p, SIDE_TABLE));
        family("bar_table", ModBlocks::wood, (c, p) -> new DecorBlock(p, BAR_TABLE));
        family("desk", ModBlocks::wood, (c, p) -> new StorageBlock(p, DESK));
        family("coffee_table", ModBlocks::wood, (c, p) -> new DecorBlock(p, COFFEE));
        family("kitchen_counter", ModBlocks::wood, (c, p) -> new CounterBlock(p));
        family("cabinet", ModBlocks::wood, (c, p) -> new StorageBlock(p));
        family("wardrobe", ModBlocks::wood, (c, p) -> new StorageBlock(p));
        family("fridge", ModBlocks::metal, (c, p) -> new StorageBlock(p));
        family("big_fridge", ModBlocks::metal, (c, p) -> new MultiStorageBlock(p));
        family("big_wardrobe", ModBlocks::wood, (c, p) -> new MultiStorageBlock(p));
        // --- cooking ---
        family("stove", ModBlocks::cooker, (c, p) -> new CookerBlock(p, false));
        family("oven", ModBlocks::cooker, (c, p) -> new CookerBlock(p, true));
        family("sink", ModBlocks::metal, (c, p) -> new SinkBlock(p));
        // --- beds ---
        family("simple_bed", ModBlocks::bed, (c, p) -> new FurnitureBed(c, p));
        family("canopy_bed", ModBlocks::bed, (c, p) -> new FurnitureBed(c, p));
        family("double_bed", ModBlocks::bed, (c, p) -> new DoubleBed(c, p));
        family("bunk_bed", ModBlocks::bed, (c, p) -> new BunkBed(c, p));
        // --- lighting ---
        family("floor_lamp", ModBlocks::lamp, (c, p) -> new LampBlock(p, b(4, 0, 4, 12, 16, 12)));
        family("table_lamp", ModBlocks::lamp, (c, p) -> new LampBlock(p, b(4, 0, 4, 12, 11, 12)));
        family("ceiling_lamp", ModBlocks::lamp, (c, p) -> new LampBlock(p, b(3, 3, 3, 13, 16, 13)));
        family("crystal_chandelier_small", ModBlocks::lamp, (c, p) -> new LampBlock(p, b(3, 1, 3, 13, 16, 13)));
        family("crystal_chandelier_medium", ModBlocks::lamp, (c, p) -> new ChandelierBlock(p, ChandelierBlock.MEDIUM));
        family("crystal_chandelier_large", ModBlocks::lamp, (c, p) -> new ChandelierBlock(p, ChandelierBlock.LARGE));
    }
}
