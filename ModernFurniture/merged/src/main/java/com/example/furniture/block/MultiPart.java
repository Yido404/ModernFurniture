package com.example.furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Helpers for furniture that spans several blocks.
 * Local axes (for facing F): x = to the right of F, y = up, z = backwards.
 * Every cell stores its own (px,py,pz) index; the "pivot" cell is the one the player clicked
 * (and the one that owns the block entity / drops the item).
 */
public final class MultiPart {
    public static final IntegerProperty PX = IntegerProperty.create("px", 0, 2);
    public static final IntegerProperty PY = IntegerProperty.create("py", 0, 1);
    public static final IntegerProperty PZ = IntegerProperty.create("pz", 0, 2);
    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");

    /** size (sx,sy,sz) and pivot index (ix,iy,iz). */
    public record Dims(int sx, int sy, int sz, int ix, int iy, int iz) {}

    public interface CellVisitor { void visit(BlockPos pos, int x, int y, int z); }

    private static BlockPos delta(BlockPos base, Direction f, int dx, int dy, int dz) {
        return base.relative(f.getClockWise(), dx).relative(f.getOpposite(), dz).above(dy);
    }

    private static BlockPos delta(BlockPos base, Direction f, int dx, int dy, int dz, boolean mirrored) {
        return base.relative(mirrored ? f.getCounterClockWise() : f.getClockWise(), dx)
                .relative(f.getOpposite(), dz).above(dy);
    }

    public static BlockPos cell(BlockPos pivot, Direction f, Dims d, int x, int y, int z) {
        return delta(pivot, f, x - d.ix(), y - d.iy(), z - d.iz());
    }

    public static BlockPos pivotOf(BlockPos pos, Direction f, Dims d, BlockState s) {
        return delta(pos, f, d.ix() - s.getValue(PX), d.iy() - s.getValue(PY), d.iz() - s.getValue(PZ));
    }

    public static BlockPos pivotOf(BlockPos pos, Direction f, Dims d, BlockState s, boolean mirrored) {
        return delta(pos, f, d.ix() - s.getValue(PX), d.iy() - s.getValue(PY), d.iz() - s.getValue(PZ), mirrored);
    }

    public static void forEach(BlockPos pivot, Direction f, Dims d, CellVisitor v) {
        for (int x = 0; x < d.sx(); x++)
            for (int y = 0; y < d.sy(); y++)
                for (int z = 0; z < d.sz(); z++)
                    v.visit(cell(pivot, f, d, x, y, z), x, y, z);
    }

    public static boolean canPlace(LevelReader level, BlockPos pivot, Direction f, Dims d) {
        boolean[] ok = {true};
        forEach(pivot, f, d, (p, x, y, z) -> {
            if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced()) ok[0] = false;
        });
        return ok[0];
    }

    /** True when the neighbouring cell that should belong to this structure is missing. */
    public static boolean shouldVanish(BlockState state, Direction f, Dims d, Direction dir, BlockState neighbor) {
        return shouldVanish(state, f, d, dir, neighbor, false);
    }

    public static boolean shouldVanish(BlockState state, Direction f, Dims d, Direction dir,
                                       BlockState neighbor, boolean mirrored) {
        int x = state.getValue(PX), y = state.getValue(PY), z = state.getValue(PZ);
        if (dir == Direction.UP) y++;
        else if (dir == Direction.DOWN) y--;
        else if (dir == (mirrored ? f.getCounterClockWise() : f.getClockWise())) x++;
        else if (dir == (mirrored ? f.getClockWise() : f.getCounterClockWise())) x--;
        else if (dir == f.getOpposite()) z++;
        else z--;
        if (x < 0 || y < 0 || z < 0 || x >= d.sx() || y >= d.sy() || z >= d.sz()) return false; // outside structure
        return !(neighbor.getBlock() == state.getBlock()
                && neighbor.getValue(PX) == x && neighbor.getValue(PY) == y && neighbor.getValue(PZ) == z
                && (!state.hasProperty(HorizontalDirectionalBlock.FACING)
                    || neighbor.getValue(HorizontalDirectionalBlock.FACING) == f)
                && (!state.hasProperty(MIRRORED) || neighbor.getValue(MIRRORED) == mirrored));
    }

    private MultiPart() {}
}
