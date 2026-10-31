package com.fantasyfeudalism.items.trophy;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
public enum TrophyKind {
    ZOMBIE(new String[]{"Resistance I"},new String[]{"Hunger drains 25% faster"}),
    SKELETON(new String[]{"+15% ranged damage"},new String[]{"Take 15% more damage"}),
    CREEPER(new String[]{"Take 40% less explosion damage","Movement sounds are 50% quieter","Hostile mobs detect you from 25% closer"},new String[]{"Deal 20% less melee damage"}),
    WITHER_SKELETON(new String[]{"Melee hits: 20% chance of Wither I for 4s"},new String[]{"Lose half a heart every 30s"}),
    STRAY(new String[]{"+15% damage","Attacks apply Slowness I for 4s","Slowness excludes skeletons and polar bears"},new String[]{"Take 25% more fire damage"}),
    BOGGED(new String[]{"+15% damage","Attacks apply Poison I for 4s","Poison does not affect undead"},new String[]{"Take 25% more damage from End creatures","Also applies to End Crystals and dragon breath"}),
    PIGLIN(new String[]{"+15% damage with gold weapons","Gold equipment loses 30% less durability","+1 armor for each gold armor piece"},new String[]{"XP orbs give no XP and cannot repair with Mending"}),
    ENDER_DRAGON(new String[]{"Endermen and endermites stay neutral until attacked","Night Vision and purple End particles"},new String[]{"Water and rain deal half a heart per second","Phantoms may spawn each night, even after sleeping"}),
    PLAYER(new String[]{"+15% damage against other players"},new String[]{"Your nametag is visible from 50% farther away"});
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
