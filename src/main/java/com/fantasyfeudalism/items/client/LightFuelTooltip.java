package com.fantasyfeudalism.items.client;
import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.lighting.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=FeudalItems.ID,value=Dist.CLIENT)
public final class LightFuelTooltip {
    @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event){
        var stack=event.getItemStack();var state=LightItemFuel.state(stack);if(state==null)return;
        if(WeatherLighting.soul(state))event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_items.fuel.unlimited").withStyle(ChatFormatting.AQUA));
        else if(LightItemFuel.stage(stack)==0)event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_items.fuel.empty").withStyle(ChatFormatting.RED));
        else event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_items.fuel.stage",LightItemFuel.stage(stack)).withStyle(ChatFormatting.GRAY));
        if(!LightItemFuel.lit(stack))event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_items.fuel.unlit").withStyle(ChatFormatting.DARK_GRAY));
    }
}
