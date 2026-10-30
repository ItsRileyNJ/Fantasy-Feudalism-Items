package com.fantasyfeudalism.items.trophy.mixin;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Mob.class)
abstract class MobDaggerMixin {
    @Inject(method="canHoldItem",at=@At("HEAD"),cancellable=true)
    private void feudalItems$noDagger(ItemStack item,CallbackInfoReturnable<Boolean> result){if(item.is(com.fantasyfeudalism.items.content.Content.BONE_DAGGER.get()))result.setReturnValue(false);}
}
