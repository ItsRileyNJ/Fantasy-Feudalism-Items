package com.fantasyfeudalism.items.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/** Reuse the trophy's item model as its recognizable HUD and inventory effect icon. */
public final class TrophyEffectIcons implements IClientMobEffectExtensions {
    private final ItemStack icon;
    public TrophyEffectIcons(ItemStack icon){this.icon=icon;}
    @Override public boolean renderInventoryIcon(MobEffectInstance instance,EffectRenderingInventoryScreen<?> screen,GuiGraphics graphics,int x,int y,int blitOffset){
        graphics.renderItem(icon,x+1,y+8);
        return true;
    }
    @Override public boolean renderGuiIcon(MobEffectInstance instance,Gui gui,GuiGraphics graphics,int x,int y,float z,float alpha){
        graphics.renderItem(icon,x+4,y+4);
        return true;
    }
}
