package com.fantasyfeudalism.items.client.mixin;

import com.fantasyfeudalism.items.client.DynamicLighting;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Entity bodies and first-person hands use this path instead of LevelRenderer's terrain light. */
@Mixin(EntityRenderer.class)
abstract class EntityLightMixin {
    @Inject(method="getPackedLightCoords",at=@At("RETURN"),cancellable=true)
    private void feudalItems$entityLight(Entity entity,float partialTick,CallbackInfoReturnable<Integer> result){
        result.setReturnValue(DynamicLighting.entityLightAt(entity,partialTick,result.getReturnValueI()));
    }
}
