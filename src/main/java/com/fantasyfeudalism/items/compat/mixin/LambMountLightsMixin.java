package com.fantasyfeudalism.items.compat.mixin;
import com.fantasyfeudalism.items.content.ModAttachments;
import com.fantasyfeudalism.items.lighting.LightItemFuel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="dev.lambdaurora.lambdynlights.resource.entity.EntityLightSources",remap=false)
abstract class LambMountLightsMixin {
    @Inject(method="getLuminance(Lnet/minecraft/world/entity/Entity;)I",at=@At("RETURN"),cancellable=true,remap=false)
    private void feudalItems$mountedLight(Entity entity,CallbackInfoReturnable<Integer> result){
        if(!com.fantasyfeudalism.items.mount.MountEquipment.supported(entity))return;
        var lamp=com.fantasyfeudalism.items.mount.MountVisuals.lantern(entity);var state=LightItemFuel.state(lamp);
        if(state!=null&&LightItemFuel.lit(lamp))result.setReturnValue(Math.max(result.getReturnValueI(),state.getLightEmission()));
    }
}
