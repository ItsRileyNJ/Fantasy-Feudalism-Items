package com.fantasyfeudalism.items.compat.mixin;
import com.fantasyfeudalism.items.lighting.LightItemFuel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Optional adapter for the supplied LambDynamicLights 4.8.11 runtime; no provider classes are linked. */
@Pseudo
@Mixin(targets="dev.lambdaurora.lambdynlights.resource.item.ItemLightSources",remap=false)
abstract class LambItemLightsMixin {
    @Inject(method="getLuminance(Lnet/minecraft/world/item/ItemStack;Z)I",at=@At("HEAD"),cancellable=true,remap=false)
    private void feudalItems$fuelAwareLight(ItemStack stack,boolean underwater,CallbackInfoReturnable<Integer> result){
        var state=LightItemFuel.state(stack);if(state!=null)result.setReturnValue(LightItemFuel.lit(stack)?state.getLightEmission():0);
    }
}
