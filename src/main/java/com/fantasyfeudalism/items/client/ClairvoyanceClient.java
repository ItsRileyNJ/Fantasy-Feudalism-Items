package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.clairvoyance.ClairvoyancePayload;
import com.fantasyfeudalism.items.clairvoyance.ClairvoyanceView;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Client-only cache. Never modifies shared entity metadata or the vanilla Glowing effect. */
@net.neoforged.fml.common.EventBusSubscriber(modid=FeudalItems.ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class ClairvoyanceClient {
    private static final ClairvoyanceView VIEW = new ClairvoyanceView();
    public static void receive(ClairvoyancePayload payload) {
        var client = Minecraft.getInstance();
        if (client.level != null) VIEW.receive(payload, client.level, client.level.dimension().location());
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
        var client=Minecraft.getInstance();
        if(client.level==null||client.player==null||!client.player.isAlive()||!client.player.hasEffect(FeudalItems.CLAIRVOYANCE))VIEW.clear();
    }
    public static boolean highlights(Entity entity) {
        var client = Minecraft.getInstance();
        return entity.isAlive() && client.level != null && entity.level() == client.level
            && client.player != null && client.player.isAlive() && client.player.hasEffect(FeudalItems.CLAIRVOYANCE)
            && VIEW.highlights(entity.getUUID(), client.level, client.level.dimension().location());
    }
    private ClairvoyanceClient() {}
}
