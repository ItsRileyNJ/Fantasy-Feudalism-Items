package com.fantasyfeudalism.items.trophy;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public final class TrophyEntity extends BlockEntity {
    public UUID owner;
    public TrophyEntity(BlockPos pos, BlockState state) { super(Trophies.BLOCK_ENTITY.get(), pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, TrophyEntity trophy) {
        if (!(level instanceof ServerLevel server)) return;
        var kind = ((TrophyBlock) state.getBlock()).kind;
        boolean active = trophy.owner != null && TrophyLedger.get(server).active(trophy.owner) == kind;
        if (level.getGameTime() % (active ? 5 : 30) != 0) return;
        ParticleOptions particle = switch (kind) {
            case ZOMBIE -> new DustParticleOptions(new Vector3f(0.8f, 0.1f, 0.1f), 0.5f);
            case SKELETON -> new DustParticleOptions(new Vector3f(0.85f, 0.85f, 0.85f), 0.5f);
            case CREEPER -> new DustParticleOptions(new Vector3f(0.1f, 0.8f, 0.1f), 0.5f);
            case WITHER_SKELETON -> ParticleTypes.ASH;
            case STRAY -> ParticleTypes.SNOWFLAKE;
            case BOGGED -> new DustParticleOptions(new Vector3f(0.4f, 0.7f, 0.1f), 0.5f);
            case PIGLIN -> new DustParticleOptions(new Vector3f(1, 0.75f, 0.1f), 0.5f);
            case ENDER_DRAGON -> ParticleTypes.PORTAL;
            case PLAYER -> ParticleTypes.ENCHANT;
        };
        server.sendParticles(particle, pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, active ? 3 : 1, 0.15, 0.1, 0.15, active ? 0.03 : 0.005);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.saveAdditional(tag, provider); if (owner != null) tag.putUUID("Owner", owner); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.loadAdditional(tag, provider); owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null; }
}
