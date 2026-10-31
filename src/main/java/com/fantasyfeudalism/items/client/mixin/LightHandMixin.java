package com.fantasyfeudalism.items.client.mixin;
import com.fantasyfeudalism.items.lighting.LightItemFuel;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ClientHooks.class)
abstract class LightHandMixin {
    @WrapOperation(method="shouldCauseReequipAnimation",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/Item;shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Z"),remap=false)
    private static boolean stableLight(Item item,ItemStack before,ItemStack after,boolean slotChanged,Operation<Boolean> original){
        if(!slotChanged&&before!=after&&LightItemFuel.sameHeldLight(before,after))return false;
        return original.call(item,before,after,slotChanged);
    }
}
