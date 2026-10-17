package com.example.furniture.client;

import com.example.furniture.entity.SeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renders nothing - the seat is invisible. */
public class SeatRenderer extends EntityRenderer<SeatEntity, EntityRenderState> {
    public SeatRenderer(EntityRendererProvider.Context ctx) { super(ctx); }
    @Override public EntityRenderState createRenderState() { return new EntityRenderState(); }
}
