package com.fantasyfeudalism.items.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Server-authoritative weather policy shared by placed, held, dropped, and mounted lights. */
public final class WeatherLighting {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue INTERVAL;
    public static final ModConfigSpec.DoubleValue TORCH_RAIN, TORCH_STORM, TORCH_WATER, LANTERN_STORM;
    static {
        var b = new ModConfigSpec.Builder();
        b.push("weather");
        INTERVAL = b.comment("Loaded game ticks between weather exposure checks (20 ticks = 1 second).")
            .defineInRange("checkIntervalTicks", 200, 1, 72000);
        TORCH_RAIN = b.defineInRange("torchRainChance", 0.10, 0.0, 1.0);
        TORCH_STORM = b.defineInRange("torchThunderstormChance", 0.25, 0.0, 1.0);
        TORCH_WATER = b.defineInRange("torchWaterChance", 1.0, 0.0, 1.0);
        LANTERN_STORM = b.defineInRange("lanternThunderstormChance", 0.05, 0.0, 1.0);
        b.pop(); SPEC = b.build();
    }
    public static boolean supported(Block block) {
        return block == Blocks.TORCH || block == Blocks.WALL_TORCH || block == Blocks.LANTERN
            || block == Blocks.SOUL_TORCH || block == Blocks.SOUL_WALL_TORCH || block == Blocks.SOUL_LANTERN;
    }
    public static boolean soul(BlockState state) {
        return state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH) || state.is(Blocks.SOUL_LANTERN);
    }
    public static boolean torch(BlockState state) { return state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH); }
    public static int interval() { return SPEC.isLoaded() ? INTERVAL.get() : 200; }
    private static double configured(ModConfigSpec.DoubleValue value, double fallback) {
        return SPEC.isLoaded() ? value.get() : fallback;
    }
    public static double chance(BlockState state, boolean rainingAtLight, boolean thunderstorm) {
        if (!supported(state.getBlock()) || soul(state) || !state.getValue(BlockStateProperties.LIT)) return 0;
        if (torch(state) && state.getValue(BlockStateProperties.WATERLOGGED)) return configured(TORCH_WATER, 1);
        if (!rainingAtLight) return 0;
        if (torch(state)) return thunderstorm ? configured(TORCH_STORM, 0.25) : configured(TORCH_RAIN, 0.10);
        return thunderstorm ? configured(LANTERN_STORM, 0.05) : 0;
    }
    public static void check(ServerLevel level, BlockPos pos, double roll) {
        BlockState state = level.getBlockState(pos);
        if (supported(state.getBlock()) && roll < chance(state, level.isRainingAt(pos), level.isThundering())) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, false));
        }
    }
    public static void schedule(ServerLevel level, BlockPos pos, BlockState state) {
        if (supported(state.getBlock()) && !soul(state)) level.scheduleTick(pos, state.getBlock(), interval());
    }
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var chunk = event.getChunk();
        // Chunk load events may run on a worker; all world mutations stay on the server thread.
        level.getServer().execute(() -> {
            if (!level.hasChunk(chunk.getPos().x, chunk.getPos().z)) return;
            chunk.findBlockLightSources((pos, state) -> { LightFuel.discover(level, pos, state); schedule(level, pos, state); });
        });
    }
    private WeatherLighting() {}
}
