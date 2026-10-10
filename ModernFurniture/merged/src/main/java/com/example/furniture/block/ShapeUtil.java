package com.example.furniture.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ShapeUtil {
    /** Rotates a shape authored for "facing north" to all four horizontal directions. */
    public static Map<Direction, VoxelShape> rotations(VoxelShape north) {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        VoxelShape cur = north;
        for (Direction d : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            map.put(d, cur);
            VoxelShape[] next = {Shapes.empty()};
            VoxelShape src = cur;
            src.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    next[0] = Shapes.or(next[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
            cur = next[0];
        }
        return map;
    }
    private ShapeUtil() {}
}
