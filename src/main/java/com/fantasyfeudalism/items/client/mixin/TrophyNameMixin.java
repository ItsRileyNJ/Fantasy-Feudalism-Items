package com.fantasyfeudalism.items.client.mixin;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LivingEntityRenderer.class)
abstract class TrophyNameMixin {
    @ModifyConstant(method="shouldShowName",constant=@Constant(floatValue=64.0f))
    private float feudalItems$visibleName(float distance,LivingEntity entity){return entity instanceof net.minecraft.world.entity.player.Player&&entity.getData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY)==com.fantasyfeudalism.items.trophy.TrophyKind.PLAYER.ordinal()?distance*1.5f:distance;}
    @ModifyConstant(method="shouldShowName",constant=@Constant(floatValue=32.0f))
    private float feudalItems$sneakingName(float distance,LivingEntity entity){return feudalItems$visibleName(distance,entity);}
}
