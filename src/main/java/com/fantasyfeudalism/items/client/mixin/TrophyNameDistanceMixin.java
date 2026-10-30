package com.fantasyfeudalism.items.client.mixin;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(EntityRenderer.class)
abstract class TrophyNameDistanceMixin {
    @Redirect(method="renderNameTag",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/client/ClientHooks;isNameplateInRenderDistance(Lnet/minecraft/world/entity/Entity;D)Z"))
    private boolean feudalItems$distance(Entity entity,double distance){
        boolean trophy=entity instanceof net.minecraft.world.entity.player.Player&&entity.getData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY)==com.fantasyfeudalism.items.trophy.TrophyKind.PLAYER.ordinal();
        return net.neoforged.neoforge.client.ClientHooks.isNameplateInRenderDistance(entity,trophy?distance/2.25:distance);
    }
}
