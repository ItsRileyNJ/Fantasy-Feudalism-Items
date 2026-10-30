package com.fantasyfeudalism.items.trophy.mixin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
abstract class HorseshoeJumpMixin {
    @Inject(method="getJumpPower(F)F",at=@At("RETURN"),cancellable=true)
    private void feudalItems$goldJumpHeight(float charge,CallbackInfoReturnable<Float> result){if((Object)this instanceof Horse horse)result.setReturnValue(com.fantasyfeudalism.items.mount.MountEquipment.jumpVelocity(horse,result.getReturnValueF()));}
}
