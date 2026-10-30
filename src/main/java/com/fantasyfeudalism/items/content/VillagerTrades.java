package com.fantasyfeudalism.items.content;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
public final class VillagerTrades {
    public static void register(VillagerTradesEvent event){if(event.getType()==VillagerProfession.FISHERMAN)event.getTrades().get(5).add((trader,random)->new MerchantOffer(new ItemCost(Items.EMERALD,18),new ItemStack(Content.FISHING_TRAP_ITEM.get()),12,30,.05f));}
}
