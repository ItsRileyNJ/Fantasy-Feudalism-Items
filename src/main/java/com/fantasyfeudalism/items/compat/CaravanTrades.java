package com.fantasyfeudalism.items.compat;
import com.fantasyfeudalism.items.mount.MountContent;
import com.fantasyfeudalism.items.spawner.SpawnerEyes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
public final class CaravanTrades {
    public static void add(MerchantOffers offers){
        add(offers,14,new ItemStack(MountContent.SADDLEBAG.get()));
        for(var eye:SpawnerEyes.ITEMS.values())add(offers,20,new ItemStack(eye.get(),4));
    }
    private static void add(MerchantOffers offers,int emeralds,ItemStack result){
        if(offers.stream().anyMatch(offer->ItemStack.isSameItemSameComponents(offer.getResult(),result)&&offer.getResult().getCount()==result.getCount()&&offer.getBaseCostA().is(Items.EMERALD)&&offer.getBaseCostA().getCount()==emeralds))return;
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD,emeralds),result,12,1,.05f));
    }
    public static void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event){
        if(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level&&event.getEntity() instanceof net.minecraft.world.entity.npc.WanderingTrader trader&&trader.getClass().getName().equals("dev.villagerscontract.CaravanTrader"))
            level.getServer().execute(()->{if(trader.isAlive())add(trader.getOffers());});
    }
    private CaravanTrades(){}
}
