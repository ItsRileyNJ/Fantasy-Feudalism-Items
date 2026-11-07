package com.fantasyfeudalism.items.client.mixin;

import com.fantasyfeudalism.items.client.ClairvoyanceClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Reveal tracked targets even when normal entity/terrain culling skips them. */
@Mixin(LevelRenderer.class)
abstract class ClairvoyanceRenderMixin {
    @WrapOperation(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
    private boolean feudalItems$renderTarget(EntityRenderDispatcher dispatcher,Entity entity,Frustum frustum,double x,double y,double z,Operation<Boolean> original){
        return ClairvoyanceClient.highlights(entity)||original.call(dispatcher,entity,frustum,x,y,z);
    }
    @WrapOperation(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/LevelRenderer;isSectionCompiled(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean feudalItems$hiddenSection(LevelRenderer renderer,BlockPos pos,Operation<Boolean> original,@Local Entity entity){
        return ClairvoyanceClient.highlights(entity)||original.call(renderer,pos);
    }
}
