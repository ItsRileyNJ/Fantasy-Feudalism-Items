package com.fantasyfeudalism.items.client.mixin;

import com.fantasyfeudalism.items.client.TrophyEffectTooltips;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Vanilla only supplies effect hover tooltips in the compact inventory layout. */
@Mixin(EffectRenderingInventoryScreen.class)
abstract class TrophyEffectTooltipMixin {
    @WrapOperation(method="renderEffects",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;IILjava/lang/Iterable;)V"))
    private void feudalItems$wideHover(EffectRenderingInventoryScreen<?> screen,GuiGraphics graphics,int x,int spacing,Iterable<MobEffectInstance> effects,Operation<Void> original,@Local(argsOnly=true,ordinal=0) int mouseX,@Local(argsOnly=true,ordinal=1) int mouseY){
        original.call(screen,graphics,x,spacing,effects);
        TrophyEffectTooltips.wideTooltip(screen,graphics,x,spacing,effects,mouseX,mouseY);
    }
}
