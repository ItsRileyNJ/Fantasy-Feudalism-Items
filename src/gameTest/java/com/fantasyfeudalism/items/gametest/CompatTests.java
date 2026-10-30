package com.fantasyfeudalism.items.gametest;
import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.compat.CaravanTrades;
import com.fantasyfeudalism.items.mount.MountContent;
import com.fantasyfeudalism.items.spawner.SpawnerEyes;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("fantasy_feudalism_compat")
@PrefixGameTestTemplate(false)
public final class CompatTests {
    @GameTest(template="empty",templateNamespace="fantasy_feudalism_compat")
    public static void mercenaries_caravan_offers(GameTestHelper h){
        try{
            var stock=Class.forName("dev.villagerscontract.CaravanStock");
            var offers=(MerchantOffers)stock.getMethod("offers",boolean.class,net.minecraft.core.HolderLookup.Provider.class).invoke(null,false,h.getLevel().registryAccess());
            h.assertTrue(offers.stream().anyMatch(offer->offer.getResult().is(MountContent.SADDLEBAG.get())&&offer.getResult().getCount()==1&&offer.getBaseCostA().is(Items.EMERALD)&&offer.getBaseCostA().getCount()==14),"Caravan trader offers saddlebag for fourteen emeralds");
            for(var eye:SpawnerEyes.ITEMS.values())h.assertTrue(offers.stream().anyMatch(offer->offer.getResult().is(eye.get())&&offer.getResult().getCount()==4&&offer.getBaseCostA().is(Items.EMERALD)&&offer.getBaseCostA().getCount()==20),"Every Spawner Eye offer has four of one matching variant for twenty emeralds");
            int count=offers.size();CaravanTrades.add(offers);h.assertTrue(count==offers.size(),"Restoring existing caravan stock does not duplicate trades");
            var other=(MerchantOffers)stock.getMethod("offers",boolean.class,net.minecraft.core.HolderLookup.Provider.class).invoke(null,true,h.getLevel().registryAccess());h.assertTrue(other.stream().anyMatch(offer->offer.getResult().is(MountContent.SADDLEBAG.get())),"Guarded caravan stock also receives saddlebag offer");
        }catch(ReflectiveOperationException exception){throw new AssertionError("Install supplied Mercenaries 1.17.1 for -Pcompat validation",exception);}
        h.succeed();
    }
    @GameTest(template="empty",templateNamespace="fantasy_feudalism_compat")
    public static void mercenary_weapon_slots_reject_bone_dagger(GameTestHelper h){
        try{
            var registry=Class.forName("dev.villagerscontract.VillagersContract");var holder=(java.util.function.Supplier<?>)registry.getField("DEFENDER").get(null);var type=(net.minecraft.world.entity.EntityType<?>)holder.get();var guard=type.create(h.getLevel());
            var menuClass=Class.forName("dev.villagerscontract.GuardMenu");var constructor=menuClass.getConstructor(int.class,net.minecraft.world.entity.player.Inventory.class,Class.forName("dev.villagerscontract.Defender"));var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var menu=(net.minecraft.world.inventory.AbstractContainerMenu)constructor.newInstance(10,player.getInventory(),guard);
            var weaponSlots=menu.slots.stream().filter(slot->slot.getClass().getName().equals("dev.villagerscontract.GuardMenu$2")).toList();
            h.assertTrue(weaponSlots.size()==3,"Mercenary weapon equipment slots found");h.assertTrue(weaponSlots.stream().noneMatch(slot->slot.mayPlace(new ItemStack(com.fantasyfeudalism.items.content.Content.BONE_DAGGER.get()))),"Bone Daggers cannot be equipped through Mercenaries weapon slots");h.assertTrue(weaponSlots.stream().anyMatch(slot->slot.mayPlace(new ItemStack(Items.IRON_SWORD))),"Ordinary swords remain valid mercenary weapons");
        }catch(ReflectiveOperationException exception){throw new AssertionError(exception);}h.succeed();
    }

}
