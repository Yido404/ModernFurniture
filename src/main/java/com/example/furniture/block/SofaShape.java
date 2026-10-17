package com.example.furniture.block;

import net.minecraft.util.StringRepresentable;

/** Which armrests a sofa piece has. Sofas placed side by side merge automatically. */
public enum SofaShape implements StringRepresentable {
    SINGLE("single"), LEFT("left"), MIDDLE("middle"), RIGHT("right");
    private final String name;
    SofaShape(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
