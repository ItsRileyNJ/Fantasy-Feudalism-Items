package com.fantasyfeudalism.items.lighting.mixin;
import com.fantasyfeudalism.items.lighting.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockItem.class)
abstract class LightPlacementMixin {
    @Inject(method="place",at=@At("HEAD"))
    private void feudalItems$capture(BlockPlaceContext context,CallbackInfoReturnable<InteractionResult> result,@com.llamalad7.mixinextras.sugar.Share("ff_light_data") com.llamalad7.mixinextras.sugar.ref.LocalRef<net.minecraft.world.item.component.CustomData> data){
        data.set(context.getItemInHand().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY));
    }
    @Inject(method="place",at=@At("RETURN"))
    private void feudalItems$keepFuel(BlockPlaceContext context,CallbackInfoReturnable<InteractionResult> result,@com.llamalad7.mixinextras.sugar.Share("ff_light_data") com.llamalad7.mixinextras.sugar.ref.LocalRef<net.minecraft.world.item.component.CustomData> captured){
        if(!result.getReturnValue().consumesAction()||!(context.getLevel() instanceof net.minecraft.server.level.ServerLevel level))return;
        var state=level.getBlockState(context.getClickedPos());if(!WeatherLighting.supported(state.getBlock()))return;
        var data=captured.get().copyTag();
        if(data.contains("ff_fuel"))LightFuel.setRemaining(level,context.getClickedPos(),data.getLong("ff_fuel"));
        if(data.getBoolean("ff_off")||LightFuel.remaining(level,context.getClickedPos())==0)level.setBlockAndUpdate(context.getClickedPos(),state.setValue(BlockStateProperties.LIT,false));
    }
}
