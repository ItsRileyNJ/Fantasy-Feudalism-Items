package com.fantasyfeudalism.items.lighting;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.world.level.block.Blocks;

public final class LightFuel extends SavedData {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue TORCH_DAYS, LANTERN_DAYS;
    static {
        var b = new ModConfigSpec.Builder();
        TORCH_DAYS = b.defineInRange("torchDays", 7.0, 0.001, 36500.0);
        LANTERN_DAYS = b.defineInRange("lanternDays", 15.0, 0.001, 36500.0);
        SPEC = b.build();
    }
    public static final class Entry {
        public long remaining; public boolean soul;
        Entry(long remaining, boolean soul) { this.remaining = remaining; this.soul = soul; }
    }
    private final Map<BlockPos, Entry> lights = new HashMap<>();
    private final Map<Long,Set<BlockPos>> soulByChunk=new HashMap<>();
    private void put(BlockPos pos,Entry entry){
        lights.put(pos,entry);long chunk=net.minecraft.world.level.ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4);
        if(entry.soul)soulByChunk.computeIfAbsent(chunk,ignored->new HashSet<>()).add(pos);else if(soulByChunk.containsKey(chunk))soulByChunk.get(chunk).remove(pos);
    }
    private void forget(BlockPos pos){lights.remove(pos);long chunk=net.minecraft.world.level.ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4);var positions=soulByChunk.get(chunk);if(positions!=null){positions.remove(pos);if(positions.isEmpty())soulByChunk.remove(chunk);}}

    public static LightFuel get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(LightFuel::new, LightFuel::load), "fantasy_feudalism_lights");
    }
    private static LightFuel load(CompoundTag tag, HolderLookup.Provider provider) {
        var data = new LightFuel();
        for (var element : tag.getList("Lights", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) element;
            data.put(BlockPos.of(entry.getLong("Pos")), new Entry(Math.max(0, entry.getLong("Fuel")), entry.getBoolean("Soul")));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var entries = new ListTag();
        lights.forEach((pos, entry) -> { var value = new CompoundTag(); value.putLong("Pos", pos.asLong()); value.putLong("Fuel", entry.remaining); value.putBoolean("Soul", entry.soul); entries.add(value); });
        tag.put("Lights", entries); return tag;
    }
    public static long capacity(BlockState state) {
        boolean torch = WeatherLighting.torch(state);
        return Math.round(24000 * (SPEC.isLoaded() ? (torch ? TORCH_DAYS.get() : LANTERN_DAYS.get()) : (torch ? 7.0 : 15.0)));
    }
    public static void placed(ServerLevel level, BlockPos pos, BlockState state, BlockState old) {
        if (!WeatherLighting.supported(state.getBlock())) return;
        var data = get(level);
        if (!state.is(old.getBlock()) || !data.lights.containsKey(pos)) {
            data.put(pos.immutable(), new Entry(capacity(state), WeatherLighting.soul(state))); data.setDirty();
        }
    }
    public static long remaining(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos); var data = get(level);
        if (!data.lights.containsKey(pos)) placed(level, pos, state, Blocks.AIR.defaultBlockState());
        var entry = data.lights.get(pos); return entry == null ? 0 : entry.remaining;
    }
    public static void setRemaining(ServerLevel level,BlockPos pos,long remaining){
        var state=level.getBlockState(pos);var data=get(level);data.put(pos.immutable(),new Entry(Math.max(0,remaining),WeatherLighting.soul(state)));data.setDirty();
    }
    public static void refill(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos); var data = get(level);
        data.put(pos.immutable(), new Entry(capacity(state), WeatherLighting.soul(state))); data.setDirty();
    }
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 20 != 0) return;
        var data = get(level); boolean changed = false;
        for (var entry : java.util.List.copyOf(data.lights.entrySet())) {
            var pos = entry.getKey();
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if (!WeatherLighting.supported(state.getBlock())) { data.forget(pos); changed = true; continue; }
            if (entry.getValue().soul || !state.getValue(BlockStateProperties.LIT)) continue;
            entry.getValue().remaining = Math.max(0, entry.getValue().remaining - 20); changed = true;
            if (entry.getValue().remaining == 0) level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, false));
        }
        if (changed) data.setDirty();
    }
    public static boolean protectedFromSun(net.minecraft.world.entity.Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        var data=get(level);var candidates=new ArrayList<BlockPos>();var origin=mob.blockPosition();
        for(int x=(origin.getX()-12)>>4;x<=(origin.getX()+12)>>4;x++)for(int z=(origin.getZ()-12)>>4;z<=(origin.getZ()+12)>>4;z++)candidates.addAll(data.soulByChunk.getOrDefault(net.minecraft.world.level.ChunkPos.asLong(x,z),Set.of()));
        for (var pos : candidates) {
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if (!WeatherLighting.soul(state) || !state.getValue(BlockStateProperties.LIT)) continue;
            int radius = state.is(Blocks.SOUL_LANTERN) ? 12 : 6;
            if (mob.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > radius * radius) continue;
            var start = net.minecraft.world.phys.Vec3.atCenterOf(pos);
            var result = level.clip(new net.minecraft.world.level.ClipContext(start, mob.getEyePosition(), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mob) {
                @Override public net.minecraft.world.phys.shapes.VoxelShape getBlockShape(BlockState s, net.minecraft.world.level.BlockGetter world, BlockPos p) {
                    return p.equals(pos) ? net.minecraft.world.phys.shapes.Shapes.empty() : super.getBlockShape(s, world, p);
                }
            });
            if (result.getType() == net.minecraft.world.phys.HitResult.Type.MISS) return true;
        }
        return false;
    }
    public static void discover(ServerLevel level, BlockPos pos, BlockState state) {
        if (WeatherLighting.supported(state.getBlock()) && !get(level).lights.containsKey(pos)) placed(level, pos, state, Blocks.AIR.defaultBlockState());
    }
}
