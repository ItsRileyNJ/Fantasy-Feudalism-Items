package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.trophy.TrophyAttunement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;

@EventBusSubscriber(modid=FeudalItems.ID,value=Dist.CLIENT)
public final class TrophyEffectTooltips {
    @SubscribeEvent public static void compactTooltip(GatherEffectScreenTooltipsEvent event){
        var lines=TrophyAttunement.tooltip(event.getEffectInstance());
        if(!lines.isEmpty()){event.getTooltip().clear();event.getTooltip().addAll(lines);}
    }
    public static void wideTooltip(EffectRenderingInventoryScreen<?> screen,GuiGraphics graphics,int x,int spacing,Iterable<MobEffectInstance> effects,int mouseX,int mouseY){
        if(mouseX<x||mouseX>=x+32)return;
        int y=screen.getGuiTop();
        for(var effect:effects){
            if(mouseY>=y&&mouseY<y+Math.min(32,spacing)){
                var lines=TrophyAttunement.tooltip(effect);
                if(!lines.isEmpty())graphics.renderTooltip(Minecraft.getInstance().font,lines,java.util.Optional.empty(),mouseX,mouseY);
                return;
            }
            y+=spacing;
        }
    }
    private TrophyEffectTooltips() {}
}
