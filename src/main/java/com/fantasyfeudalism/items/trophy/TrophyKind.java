package com.fantasyfeudalism.items.trophy;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
public enum TrophyKind {
    ZOMBIE(new String[]{"Gain Resistance I."},new String[]{"Hunger depletes 25% faster."}),
    SKELETON(new String[]{"Deal 15% more ranged damage."},new String[]{"Take 15% more damage."}),
    CREEPER(new String[]{"Take 40% less explosion damage.","Movement sounds are 50% quieter.","Hostile mobs must be 25% closer to detect you."},new String[]{"Deal 20% less melee damage."}),
    WITHER_SKELETON(new String[]{"Melee hits have a 20% chance to inflict Wither I for 4 seconds."},new String[]{"Lose half a heart every 30 seconds."}),
    STRAY(new String[]{"Deal 15% more damage.","Attacks inflict Slowness I for 4 seconds.","Slowness does not affect skeletons or polar bears."},new String[]{"Take 25% more fire damage."}),
    BOGGED(new String[]{"Deal 15% more damage.","Attacks inflict Poison I for 4 seconds.","Poison does not affect undead mobs."},new String[]{"Take 25% more damage from End creatures.","This also applies to End Crystals and dragon breath."}),
    PIGLIN(new String[]{"Deal 15% more damage with gold weapons.","Gold equipment loses 30% less durability.","Gain 1 armor point per piece of gold armor."},new String[]{"XP orbs grant no XP and cannot repair equipment through Mending."}),
    ENDER_DRAGON(new String[]{"Endermen and endermites remain neutral until you attack them.","Gain Night Vision and emit purple End particles."},new String[]{"Water and rain deal half a heart of damage per second.","Phantoms may spawn each night, even after you sleep."}),
    PLAYER(new String[]{"Deal 15% more damage to other players."},new String[]{"Your nametag is visible from 50% farther away, even while crouching."});
    public final String[] benefits,drawbacks;
    TrophyKind(String[] benefits,String[] drawbacks){this.benefits=benefits;this.drawbacks=drawbacks;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    public void appendTooltip(List<Component> lines){
        lines.add(Component.translatable("tooltip.fantasy_feudalism_items.trophy.bonus").withStyle(ChatFormatting.GREEN));
        for(int i=0;i<benefits.length;i++)lines.add(Component.translatable("tooltip.fantasy_feudalism_items.trophy."+id()+".benefit."+i).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.fantasy_feudalism_items.trophy.drawback").withStyle(ChatFormatting.RED));
        for(int i=0;i<drawbacks.length;i++)lines.add(Component.translatable("tooltip.fantasy_feudalism_items.trophy."+id()+".drawback."+i).withStyle(ChatFormatting.GRAY));
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.fantasy_feudalism_items.trophy.attune").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("tooltip.fantasy_feudalism_items.trophy.limit").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.fantasy_feudalism_items.trophy.permanent").withStyle(ChatFormatting.GOLD));
    }
}
