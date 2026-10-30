package com.fantasyfeudalism.items.gametest;
import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.trophy.*;
import com.fantasyfeudalism.items.content.*;
import com.fantasyfeudalism.items.mount.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class TrophyTests {
    private static ServerPlayer player(GameTestHelper h){
        var profile=new com.mojang.authlib.GameProfile(UUID.randomUUID(),"trophy-test");
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,ClientInformation.createDefault());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,new net.minecraft.server.network.CommonListenerCookie(profile,0,ClientInformation.createDefault(),false,net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE));
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        player.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(1,2,1))));return player;
    }
    private static ServerPlayer onlinePlayer(GameTestHelper h){
        var profile=new com.mojang.authlib.GameProfile(UUID.randomUUID(),"online-test");
        var cookie=new net.minecraft.server.network.CommonListenerCookie(profile,0,ClientInformation.createDefault(),false,net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,ClientInformation.createDefault());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        // Configure mod channels before login events send Moonlight/other mod payloads.
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,player,cookie);
        return player;
    }
    private static void select(ServerPlayer player,TrophyKind kind){var ledger=TrophyLedger.get(player.serverLevel());ledger.recordPlacement(player.getUUID(),kind);ledger.attune(player.getUUID(),kind);}
    @GameTest(template="empty")
    public static void trophies_are_owner_only_and_destruction_ends_attunement(GameTestHelper h){
        var owner=h.makeMockPlayer(GameType.SURVIVAL);var outsider=h.makeMockPlayer(GameType.SURVIVAL);var pos=h.absolutePos(new net.minecraft.core.BlockPos(2,2,2));var block=Trophies.BLOCKS.get(TrophyKind.CREEPER).get();
        h.getLevel().setBlockAndUpdate(pos,block.defaultBlockState());block.setPlacedBy(h.getLevel(),pos,block.defaultBlockState(),owner,new ItemStack(block));
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        h.getLevel().getBlockState(pos).useWithoutItem(h.getLevel(),outsider,hit);h.assertTrue(TrophyLedger.get(h.getLevel()).active(outsider.getUUID())==null,"Non-owner cannot attune");
        h.getLevel().getBlockState(pos).useWithoutItem(h.getLevel(),owner,hit);h.assertTrue(TrophyLedger.get(h.getLevel()).active(owner.getUUID())==TrophyKind.CREEPER,"Owner can attune");
        h.getLevel().removeBlock(pos,false);h.assertTrue(TrophyLedger.get(h.getLevel()).active(owner.getUUID())==null,"Destroying trophy ends attunement");h.assertTrue(TrophyLedger.get(h.getLevel()).hasPlaced(owner.getUUID(),TrophyKind.CREEPER),"Destruction does not erase permanent placement history");
        var context=new BlockPlaceContext(owner,InteractionHand.MAIN_HAND,new ItemStack(block),hit);h.assertTrue(((BlockItem)block.asItem()).place(context)==InteractionResult.FAIL,"Same owner cannot place another of destroyed trophy type");h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_combat_percentages(GameTestHelper h){
        var player=player(h);var victim=h.spawnWithNoFreeWill(EntityType.COW,3,2,3);
        select(player,TrophyKind.STRAY);var hit=new LivingIncomingDamageEvent(victim,new DamageContainer(h.getLevel().damageSources().playerAttack(player),10));TrophyEffects.damage(hit);h.assertTrue(Math.abs(hit.getAmount()-11.5)<.001,"Stray gives fifteen percent damage bonus");h.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Stray applies Slowness");
        select(player,TrophyKind.BOGGED);var undead=h.spawnWithNoFreeWill(EntityType.ZOMBIE,4,2,3);var poison=new LivingIncomingDamageEvent(undead,new DamageContainer(h.getLevel().damageSources().playerAttack(player),10));TrophyEffects.damage(poison);h.assertFalse(undead.hasEffect(net.minecraft.world.effect.MobEffects.POISON),"Bogged cannot poison undead");
        select(player,TrophyKind.CREEPER);var melee=new LivingIncomingDamageEvent(victim,new DamageContainer(h.getLevel().damageSources().playerAttack(player),10));TrophyEffects.damage(melee);h.assertTrue(Math.abs(melee.getAmount()-8)<.001,"Creeper reduces melee damage by twenty percent");
        select(player,TrophyKind.SKELETON);var arrow=new Arrow(h.getLevel(),player,new ItemStack(Items.ARROW),null);var ranged=new LivingIncomingDamageEvent(victim,new DamageContainer(h.getLevel().damageSources().arrow(arrow,player),10));TrophyEffects.damage(ranged);h.assertTrue(Math.abs(ranged.getAmount()-11.5)<.001,"Skeleton boosts ranged damage");
        var taken=new LivingIncomingDamageEvent(player,new DamageContainer(h.getLevel().damageSources().generic(),10));TrophyEffects.damage(taken);h.assertTrue(Math.abs(taken.getAmount()-11.5)<.001,"Skeleton drawback increases incoming damage");h.succeed();
    }
    @GameTest(template="empty")
    public static void piglin_xp_is_destroyed_before_mending(GameTestHelper h){
        var player=player(h);select(player,TrophyKind.PIGLIN);player.experienceLevel=5;var orb=new ExperienceOrb(h.getLevel(),player.getX(),player.getY(),player.getZ(),10);var event=new PlayerXpEvent.PickupXp(player,orb);TrophyEffects.xp(event);h.assertTrue(event.isCanceled()&&orb.isRemoved(),"XP pickup canceled and orb destroyed before Mending");h.assertTrue(player.experienceLevel==5,"Destroyed orb grants no XP");h.succeed();
    }
    @GameTest(template="empty")
    public static void head_drop_probabilities_are_configurable(GameTestHelper h){
        h.assertTrue(Math.abs(HeadDrops.CHANCES.get(3).get()-1.1)<.00001,"Looting III default is one-point-one percent");
        double previous=HeadDrops.CHANCES.getFirst().get();
        try{
            var mob=h.spawnWithNoFreeWill(EntityType.ZOMBIE,2,2,2);HeadDrops.CHANCES.getFirst().set(100.0);var drops=new ArrayList<ItemEntity>();HeadDrops.drops(new LivingDropsEvent(mob,h.getLevel().damageSources().generic(),drops,false));h.assertTrue(drops.size()==1&&drops.getFirst().getItem().is(Items.ZOMBIE_HEAD),"Any kill can drop a configurable rare head");
            HeadDrops.CHANCES.getFirst().set(0.0);drops.clear();HeadDrops.drops(new LivingDropsEvent(mob,h.getLevel().damageSources().generic(),drops,false));h.assertTrue(drops.isEmpty(),"Zero chance disables rare head drops");
        }finally{HeadDrops.CHANCES.getFirst().set(previous);}h.succeed();
    }
    @GameTest(template="empty")
    public static void saddlebag_death_drop_preserves_inventory(GameTestHelper h){
        var donkey=h.spawnWithNoFreeWill(EntityType.DONKEY,2,2,2);var bag=new ItemStack(MountContent.SADDLEBAG.get());bag.set(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,21))));MountEquipment.set(donkey,4,bag);MountEquipment.set(donkey,3,new ItemStack(Items.SOUL_LANTERN));
        var drops=new ArrayList<ItemEntity>();MountEquipment.drops(new LivingDropsEvent(donkey,h.getLevel().damageSources().generic(),drops,true));h.assertTrue(drops.size()==2,"Equipment drops as bag and lantern only");var stored=drops.stream().map(ItemEntity::getItem).filter(stack->stack.is(MountContent.SADDLEBAG.get())).findFirst().orElseThrow();h.assertTrue(stored.get(net.minecraft.core.component.DataComponents.CONTAINER).getStackInSlot(0).getCount()==21,"Bag death drop preserves its complete inventory");h.assertTrue(MountEquipment.item(donkey,4).isEmpty(),"Dropped equipment removed from mount state");h.succeed();
    }
    @GameTest(template="empty")
    public static void block_placement_preserves_light_item_fuel(GameTestHelper h){
        var player=h.makeMockPlayer(GameType.SURVIVAL);var floor=h.absolutePos(new BlockPos(2,1,2));h.getLevel().setBlockAndUpdate(floor,Blocks.STONE.defaultBlockState());var stack=new ItemStack(Items.TORCH);com.fantasyfeudalism.items.lighting.LightItemFuel.set(stack,123,false);
        var hit=new BlockHitResult(Vec3.atCenterOf(floor),Direction.UP,floor,false);var context=new BlockPlaceContext(player,InteractionHand.MAIN_HAND,stack,hit);
        var result=((BlockItem)Items.TORCH).place(context);h.assertTrue(result.consumesAction(),"Torch placement succeeds");var pos=floor.above();h.assertTrue(com.fantasyfeudalism.items.lighting.LightFuel.remaining(h.getLevel(),pos)==123,"Placement preserves remaining fuel even when last item is consumed");h.assertFalse(h.getLevel().getBlockState(pos).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT),"An extinguished item remains extinguished after placement");h.succeed();
    }
    @GameTest(template="empty")
    public static void creeper_detection_stacks_with_sneaking(GameTestHelper h){
        var player=player(h);var observer=h.spawnWithNoFreeWill(EntityType.ZOMBIE,2,2,2);
        double baseline=player.getVisibilityPercent(observer);select(player,TrophyKind.CREEPER);
        h.assertTrue(Math.abs(player.getVisibilityPercent(observer)-baseline*.75)<.00001,"Creeper reduces hostile detection by twenty-five percent");
        player.setShiftKeyDown(true);h.assertTrue(Math.abs(player.getVisibilityPercent(observer)-baseline*.8*.75)<.00001,"Creeper detection reduction stacks with vanilla sneaking");
        h.assertTrue(Math.abs(player.getVisibilityPercent(EntityType.COW.create(h.getLevel()))-baseline*.8)<.00001,"Passive animal detection is unchanged");h.succeed();
    }
    @GameTest(template="empty")
    public static void fisherman_trade_and_optional_loot_gate(GameTestHelper h){
        var trades=new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<java.util.List<net.minecraft.world.entity.npc.VillagerTrades.ItemListing>>();for(int level=1;level<=5;level++)trades.put(level,new ArrayList<>());
        com.fantasyfeudalism.items.content.VillagerTrades.register(new net.neoforged.neoforge.event.village.VillagerTradesEvent(trades,net.minecraft.world.entity.npc.VillagerProfession.FISHERMAN,h.getLevel().registryAccess()));
        var offer=trades.get(5).getFirst().getOffer(EntityType.VILLAGER.create(h.getLevel()),net.minecraft.util.RandomSource.create(1));h.assertTrue(offer.getBaseCostA().getCount()==18&&offer.getResult().is(Content.FISHING_TRAP_ITEM.get()),"Master Fisherman sells trap for eighteen emeralds");h.assertTrue(trades.get(4).isEmpty(),"Fishing trap does not appear before master level");
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(h.getLevel()).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.EMPTY);
        var context=new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(Optional.empty());
        var modifier=new com.fantasyfeudalism.items.mount.SaddlebagLoot(new net.minecraft.world.level.storage.loot.predicates.LootItemCondition[0]);var loot=modifier.apply(new it.unimi.dsi.fastutil.objects.ObjectArrayList<>(),context);
        h.assertTrue(loot.size()==(net.neoforged.fml.ModList.get().isLoaded("villagerscontract")?1:0),"Village saddlebag loot requires optional Mercenaries");h.succeed();
    }

    @GameTest(template="empty")
    public static void existing_donkey_chest_cargo_migrates(GameTestHelper h){
        var donkey=h.spawnWithNoFreeWill(EntityType.DONKEY,2,2,2);donkey.getSlot(499).set(new ItemStack(Items.CHEST));donkey.getSlot(500).set(new ItemStack(Items.EMERALD,19));
        MountEquipment.join(new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(donkey,h.getLevel(),true));
        h.assertFalse(donkey.hasChest(),"Legacy chest attachment is removed after migration");var bag=MountEquipment.item(donkey,4);h.assertTrue(bag.is(MountContent.SADDLEBAG.get())&&bag.get(net.minecraft.core.component.DataComponents.CONTAINER).getStackInSlot(0).getCount()==19,"Legacy cargo is preserved inside a saddlebag");h.succeed();
    }
    @GameTest(template="empty")
    public static void allegiance_first_player_wins_and_effect_end_releases(GameTestHelper h){
        var first=onlinePlayer(h);var second=onlinePlayer(h);var cow=h.spawnWithNoFreeWill(EntityType.COW,2,2,2);first.setPos(cow.position());second.setPos(cow.position());
        try{
            first.addEffect(new net.minecraft.world.effect.MobEffectInstance(AnimalAllegiance.EFFECT,1200));second.addEffect(new net.minecraft.world.effect.MobEffectInstance(AnimalAllegiance.EFFECT,1200));
            h.assertTrue(AnimalAllegiance.claim(cow,first),"First affected player attracts passive animal");h.assertFalse(AnimalAllegiance.claim(cow,second),"Second affected player cannot steal attracted animal");
            cow.setPos(first.getX()+40,first.getY(),first.getZ());h.assertTrue(AnimalAllegiance.owner(cow)==first,"Animal retains its first owner outside trigger radius");
            first.removeEffect(AnimalAllegiance.EFFECT);h.assertFalse(AnimalAllegiance.claimed(cow),"Expiry removes forced following");cow.setPos(second.position());h.assertTrue(AnimalAllegiance.claim(cow,second),"A new affected player may attract after previous effect ends");
        }finally{h.getLevel().getServer().getPlayerList().remove(first);h.getLevel().getServer().getPlayerList().remove(second);}h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_death_clears_bonus_but_preserves_placement(GameTestHelper h){
        var player=player(h);select(player,TrophyKind.ZOMBIE);TrophyEffects.clone(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(player,player,true));
        h.assertTrue(TrophyEffects.active(player)==null,"Death clears active trophy");h.assertTrue(TrophyLedger.get(h.getLevel()).hasPlaced(player.getUUID(),TrophyKind.ZOMBIE),"Death preserves permanent placement restriction");h.succeed();
    }

    @GameTest(template="empty")
    public static void trophy_damage_uses_actual_weapon_and_other_players(GameTestHelper h){
        var player=player(h);var other=player(h);var cow=h.spawnWithNoFreeWill(EntityType.COW,2,2,2);var arrow=new Arrow(h.getLevel(),player,new ItemStack(Items.ARROW),new ItemStack(Items.BOW));
        select(player,TrophyKind.PLAYER);var self=new LivingIncomingDamageEvent(player,new DamageContainer(h.getLevel().damageSources().arrow(arrow,player),10));TrophyEffects.damage(self);h.assertTrue(Math.abs(self.getAmount()-10)<.001,"Player trophy never increases damage to its own owner");
        var pvp=new LivingIncomingDamageEvent(other,new DamageContainer(h.getLevel().damageSources().playerAttack(player),10));TrophyEffects.damage(pvp);h.assertTrue(Math.abs(pvp.getAmount()-11.5)<.001,"Player trophy increases damage to other players");
        select(player,TrophyKind.PIGLIN);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GOLDEN_SWORD));var bow=new LivingIncomingDamageEvent(cow,new DamageContainer(h.getLevel().damageSources().arrow(arrow,player),10));TrophyEffects.damage(bow);h.assertTrue(Math.abs(bow.getAmount()-10)<.001,"Switching to gold after firing does not turn bow damage into gold-weapon damage");h.succeed();
    }
    @GameTest(template="empty")
    public static void wither_cost_waits_thirty_active_seconds(GameTestHelper h){
        var player=player(h);
        // This test advances trophy ticks directly, so expire vanilla login protection explicitly.
        try{var field=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");field.setAccessible(true);field.setInt(player,0);}catch(ReflectiveOperationException e){throw new AssertionError(e);}
        select(player,TrophyKind.WITHER_SKELETON);float health=player.getHealth();
        for(int i=1;i<30;i++){player.tickCount=i*20;TrophyEffects.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));}
        h.assertTrue(player.getHealth()==health,"Wither trophy does not hurt before thirty active seconds");player.tickCount=600;TrophyEffects.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));h.assertTrue(Math.abs(player.getHealth()-(health-1))<.001,"Wither trophy costs half a heart after thirty active seconds");h.succeed();
    }

}
