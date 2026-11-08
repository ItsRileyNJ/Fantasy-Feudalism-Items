package com.fantasyfeudalism.items.mount;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class Horseshoe extends Item {
    public final double speed, fall;
    public final boolean gold;
    private final int color;
    public Horseshoe(double speed, double fall, boolean gold, Properties properties) { this(speed,fall,gold,0xFFFFFFFF,properties); }
    public Horseshoe(double speed, double fall, boolean gold, int color, Properties properties) {
        super(properties.stacksTo(1));this.speed=speed;this.fall=fall;this.gold=gold;
        this.color=net.minecraft.util.FastColor.ARGB32.opaque(color);
    }
    public static int itemColor(ItemStack stack,int layer){
        return layer==0&&stack.getItem() instanceof Horseshoe shoe?shoe.color:-1;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+" + Math.round(speed*100) + "% horse speed; " + Math.round(fall*100) + "% less fall damage"));
        if (gold) tooltip.add(Component.literal("+10% jump height"));
    }
}
