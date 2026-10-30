package com.fantasyfeudalism.items.clairvoyance;

import com.fantasyfeudalism.items.FeudalItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClairvoyanceTracker {
    private static final java.util.Set<ServerPlayer> ACTIVE_VIEWERS = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    public static boolean eligible(ServerPlayer viewer, LivingEntity target, double radius) {
        if (target == viewer || !target.isAlive() || !(target instanceof Mob || target instanceof net.minecraft.world.entity.player.Player)) return false;
        if (target instanceof EnderDragon || target instanceof WitherBoss || target.getType().is(Tags.EntityTypes.BOSSES)) return false;
        if (target instanceof ServerPlayer player && (player.isCreative() || player.server.getPlayerList().isOp(player.getGameProfile()))) return false;
        return target.distanceToSqr(viewer) <= radius * radius;
    }

    public static List<UUID> targets(ServerPlayer viewer) {
        if (!viewer.hasEffect(FeudalItems.CLAIRVOYANCE) || !viewer.isAlive()) return List.of();
        double radius = ClairvoyanceConfig.radius();
        return viewer.serverLevel().getEntitiesOfClass(LivingEntity.class, viewer.getBoundingBox().inflate(radius),
            target -> eligible(viewer, target, radius)).stream().map(Entity::getUUID).toList();
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 5 != 0) return;
        boolean active = player.isAlive() && player.hasEffect(FeudalItems.CLAIRVOYANCE);
        if (active) ACTIVE_VIEWERS.add(player);
        else if (!ACTIVE_VIEWERS.remove(player)) return;
        // Empty updates clear outlines after milk, expiry, death, respawn, or dimension travel.
        PacketDistributor.sendToPlayer(player, new ClairvoyancePayload(player.level().dimension().location(), targets(player)));
    }
    private ClairvoyanceTracker() {}
}
