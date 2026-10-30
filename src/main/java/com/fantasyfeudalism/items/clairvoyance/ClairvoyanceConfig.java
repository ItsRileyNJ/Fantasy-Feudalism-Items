package com.fantasyfeudalism.items.clairvoyance;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClairvoyanceConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue RADIUS;
    public static final ModConfigSpec.IntValue DURATION_SECONDS;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("clairvoyance");
        RADIUS = builder.comment("Detection radius in blocks. Targets must also be tracked by the client.")
            .defineInRange("radiusBlocks", 24, 1, 128);
        DURATION_SECONDS = builder.comment("Base potion duration in seconds. Splash, lingering and arrows use vanilla duration scaling.")
            .defineInRange("durationSeconds", 120, 1, 86400);
        builder.pop();
        SPEC = builder.build();
    }
    public static int radius() { return SPEC.isLoaded() ? RADIUS.get() : 24; }
    public static int durationTicks() { return (SPEC.isLoaded() ? DURATION_SECONDS.get() : 120) * 20; }
    private ClairvoyanceConfig() {}
}
