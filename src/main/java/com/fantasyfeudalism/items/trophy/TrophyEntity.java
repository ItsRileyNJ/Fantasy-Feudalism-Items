package com.fantasyfeudalism.items.trophy;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TrophyEntity extends BlockEntity {
    public UUID owner;
    public TrophyEntity(BlockPos pos, BlockState state) { super(Trophies.BLOCK_ENTITY.get(), pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, TrophyEntity trophy) {
        if (!(level instanceof ServerLevel server)) return;
        var kind = ((TrophyBlock) state.getBlock()).kind;
        boolean active = trophy.owner != null && TrophyLedger.get(server).active(trophy.owner) == kind;
        var profile=active?TrophyParticles.ACTIVE_PROFILE:TrophyParticles.IDLE_PROFILE;
        if(level.getGameTime()%profile.interval()!=0)return;
        var particle=ColorParticleOption.create(active?TrophyParticles.ACTIVE.get():TrophyParticles.IDLE.get(),TrophyParticles.color(kind));
        server.sendParticles(particle,pos.getX()+.5,pos.getY()+.93,pos.getZ()+.5,profile.count(),active?.20:.12,.025,active?.20:.12,profile.jitter());
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.saveAdditional(tag, provider); if (owner != null) tag.putUUID("Owner", owner); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.loadAdditional(tag, provider); owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null; }
}
