package com.fantasyfeudalism.items.trophy.mixin;
import com.fantasyfeudalism.items.trophy.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
abstract class CreeperVisibilityMixin {
    @Inject(method="getVisibilityPercent",at=@At("RETURN"),cancellable=true)
    private void feudalItems$reducedDetection(Entity observer,CallbackInfoReturnable<Double> result){
        if((Object)this instanceof Player player&&observer instanceof net.minecraft.world.entity.monster.Enemy){
            boolean active=player instanceof net.minecraft.server.level.ServerPlayer?TrophyEffects.active(player)==TrophyKind.CREEPER:player.getData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY)==TrophyKind.CREEPER.ordinal();
            if(active)result.setReturnValue(result.getReturnValueD()*.75);
        }
    }
}
