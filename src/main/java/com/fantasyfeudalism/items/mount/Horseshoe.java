package com.fantasyfeudalism.items.mount;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class Horseshoe extends Item {
    public final double speed, fall;
    public final boolean gold;
    public Horseshoe(double speed, double fall, boolean gold, Properties properties) { super(properties.stacksTo(1)); this.speed=speed; this.fall=fall; this.gold=gold; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+" + Math.round(speed*100) + "% horse speed; " + Math.round(fall*100) + "% less fall damage"));
        if (gold) tooltip.add(Component.literal("+10% jump height"));
    }
}
