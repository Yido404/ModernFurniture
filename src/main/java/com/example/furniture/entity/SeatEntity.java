package com.example.furniture.entity;

import com.example.furniture.block.SeatBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Invisible, intangible entity the player rides while sitting on furniture. */
public class SeatEntity extends Entity {
    private boolean heals;

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setHeals(boolean heals) { this.heals = heals; }

    /** The entity type is serialisable (needed so players may ride it) but we never want it on disk. */
    @Override public boolean shouldBeSaved() { return false; }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(ValueInput in) {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            if (getPassengers().isEmpty() || !(server.getBlockState(blockPosition()).getBlock() instanceof SeatBlock)) {
                discard();
                return;
            }
            // recliner: relax and slowly recover 1 HP every 5 seconds
            if (heals && tickCount % 100 == 0 && getFirstPassenger() instanceof Player p) p.heal(1.0f);
        }
    }
}
