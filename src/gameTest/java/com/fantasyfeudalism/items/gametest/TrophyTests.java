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
        h.getLevel().removeBlock(pos,false);h.assertTrue(TrophyLedger.get(h.getLevel()).active(owner.getUUID())==null,"Destroying trophy ends attunement");h.assertFalse(TrophyLedger.get(h.getLevel()).hasPlaced(owner.getUUID(),TrophyKind.CREEPER),"Destruction releases the owner's placement slot");
        var floor=pos.below();h.getLevel().setBlockAndUpdate(floor,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        var context=new BlockPlaceContext(owner,InteractionHand.MAIN_HAND,new ItemStack(block),new BlockHitResult(Vec3.atCenterOf(floor),Direction.UP,floor,false));
        h.assertTrue(((BlockItem)block.asItem()).place(context).consumesAction(),"Owner can place a new trophy of the same type after destruction");
        h.assertTrue(TrophyLedger.get(h.getLevel()).hasPlaced(owner.getUUID(),TrophyKind.CREEPER),"Replacement reserves its type again");h.succeed();
    }
    @GameTest(template="empty")
    public static void right_clicking_another_owned_trophy_replaces_attunement(GameTestHelper h){
        var owner=onlinePlayer(h);var stranger=h.makeMockPlayer(GameType.SURVIVAL);
        var first=h.absolutePos(new net.minecraft.core.BlockPos(2,2,2));var second=h.absolutePos(new net.minecraft.core.BlockPos(4,2,2));
        var creeper=Trophies.BLOCKS.get(TrophyKind.CREEPER).get();var skeleton=Trophies.BLOCKS.get(TrophyKind.SKELETON).get();
        h.getLevel().setBlockAndUpdate(first,creeper.defaultBlockState());creeper.setPlacedBy(h.getLevel(),first,creeper.defaultBlockState(),owner,new ItemStack(creeper));
        h.getLevel().setBlockAndUpdate(second,skeleton.defaultBlockState());skeleton.setPlacedBy(h.getLevel(),second,skeleton.defaultBlockState(),owner,new ItemStack(skeleton));
        var firstHit=new BlockHitResult(Vec3.atCenterOf(first),Direction.UP,first,false);var secondHit=new BlockHitResult(Vec3.atCenterOf(second),Direction.UP,second,false);
        h.getLevel().getBlockState(first).useWithoutItem(h.getLevel(),owner,firstHit);
        h.assertTrue(TrophyEffects.active(owner)==TrophyKind.CREEPER,"Empty-hand right-click attunes first trophy");
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.EMERALD));
        var result=h.getLevel().getBlockState(second).useItemOn(owner.getMainHandItem(),h.getLevel(),owner,InteractionHand.MAIN_HAND,secondHit);
        h.assertTrue(result.consumesAction(),"Trophy handles right-click while holding an item");
        h.assertTrue(TrophyEffects.active(owner)==TrophyKind.SKELETON,"Right-clicking another trophy replaces the active selection");
        h.assertTrue(owner.getData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY)==TrophyKind.SKELETON.ordinal(),"New selection is synchronized to the client");
        h.assertTrue(owner.getPersistentData().getString("fantasy_feudalism_trophy").equals("SKELETON"),"Persistent selection switches too");
        var victim=h.spawnWithNoFreeWill(EntityType.COW,3,2,3);
        var damage=new LivingIncomingDamageEvent(victim,new DamageContainer(h.getLevel().damageSources().playerAttack(owner),10));TrophyEffects.damage(damage);
        h.assertTrue(Math.abs(damage.getAmount()-10)<.001,"Previous Creeper melee drawback stops immediately");
        owner.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.STICK));
        h.getLevel().getBlockState(first).useItemOn(owner.getOffhandItem(),h.getLevel(),owner,InteractionHand.OFF_HAND,firstHit);
        h.assertTrue(TrophyEffects.active(owner)==TrophyKind.CREEPER,"Offhand right-click can switch back");
        h.getLevel().getBlockState(second).useItemOn(new ItemStack(Items.EMERALD),h.getLevel(),stranger,InteractionHand.MAIN_HAND,secondHit);
        h.assertTrue(TrophyLedger.get(h.getLevel()).active(stranger.getUUID())==null,"Another player's trophy cannot be selected");
        h.assertTrue(TrophyEffects.active(owner)==TrophyKind.CREEPER,"Stranger cannot change owner's selection");
        h.getLevel().removeBlock(second,false);
        h.assertTrue(TrophyEffects.active(owner)==TrophyKind.CREEPER,"Destroying an inactive trophy preserves the current attunement");
        h.assertTrue(owner.hasEffect(TrophyAttunement.EFFECTS.get(TrophyKind.CREEPER)),"Inactive trophy destruction preserves the current indicator");
        h.getLevel().removeBlock(first,false);
        h.assertTrue(TrophyAttunement.EFFECTS.values().stream().noneMatch(owner::hasEffect),"Destroying the active trophy removes its indicator immediately");h.succeed();
    }
    @GameTest(template="empty")
    public static void switching_trophies_clears_previous_night_vision(GameTestHelper h){
        var owner=player(h);var dragon=Trophies.BLOCKS.get(TrophyKind.ENDER_DRAGON).get();var creeper=Trophies.BLOCKS.get(TrophyKind.CREEPER).get();
        var first=h.absolutePos(new net.minecraft.core.BlockPos(2,2,2));var second=h.absolutePos(new net.minecraft.core.BlockPos(4,2,2));
        h.getLevel().setBlockAndUpdate(first,dragon.defaultBlockState());dragon.setPlacedBy(h.getLevel(),first,dragon.defaultBlockState(),owner,new ItemStack(dragon));
        h.getLevel().setBlockAndUpdate(second,creeper.defaultBlockState());creeper.setPlacedBy(h.getLevel(),second,creeper.defaultBlockState(),owner,new ItemStack(creeper));
        h.getLevel().getBlockState(first).useWithoutItem(h.getLevel(),owner,new BlockHitResult(Vec3.atCenterOf(first),Direction.UP,first,false));
        owner.tickCount=20;TrophyEffects.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
        h.assertTrue(owner.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"Dragon trophy grants its passive bonus");
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
        h.getLevel().getBlockState(second).useItemOn(owner.getMainHandItem(),h.getLevel(),owner,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(second),Direction.UP,second,false));
        h.assertFalse(owner.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"Switching removes the previous trophy's own passive bonus immediately");
        h.assertTrue(TrophyEffects.active(owner)==TrophyKind.CREEPER,"New trophy is the only active selection");h.succeed();
    }
    @GameTest(template="empty")
    public static void player_trophy_keeps_full_nametag_drawback_while_crouched(GameTestHelper h){
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        h.assertTrue(player.isDiscrete(),"Crouching normally uses discrete nametag rendering");
        h.assertTrue(TrophyNameRules.discrete(player,player.isDiscrete()),"Unattuned players retain vanilla crouching visibility");
        player.setData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY,TrophyKind.PLAYER.ordinal());
        h.assertFalse(TrophyNameRules.discrete(player,player.isDiscrete()),"Player Trophy prevents crouching from hiding or dimming the nametag");
        h.assertTrue(64*TrophyNameRules.distanceMultiplier(player)==96,"Crouched attuned players keep the full 96-block name range");
        player.setShiftKeyDown(false);
        h.assertFalse(TrophyNameRules.discrete(player,player.isDiscrete()),"Standing attuned players retain normal visibility");
        h.assertTrue(64*TrophyNameRules.distanceMultiplier(player)==96,"Standing and crouching use the same extended range");
        player.setShiftKeyDown(true);player.setData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY,TrophyKind.CREEPER.ordinal());
        h.assertTrue(TrophyNameRules.discrete(player,player.isDiscrete()),"Switching away restores crouching visibility immediately");
        h.assertTrue(TrophyNameRules.distanceMultiplier(player)==1,"Other trophies do not extend nametag range");
        var cow=h.spawnWithNoFreeWill(EntityType.COW,3,2,3);cow.setData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY,TrophyKind.PLAYER.ordinal());
        h.assertTrue(TrophyNameRules.discrete(cow,true)&&TrophyNameRules.distanceMultiplier(cow)==1,"The drawback applies only to players");h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_status_indicator_matches_each_attunement(GameTestHelper h){
        var player=player(h);
        for(var kind:TrophyKind.values()){
            select(player,kind);TrophyEffects.changed(player);
            h.assertTrue(TrophyAttunement.EFFECTS.values().stream().filter(player::hasEffect).count()==1,"Exactly one trophy indicator is displayed after switching");
            var effect=player.getEffect(TrophyAttunement.EFFECTS.get(kind));
            h.assertTrue(effect!=null&&effect.showIcon()&&!effect.isVisible(),"Selected trophy has a visible status icon without extra particles");
            h.assertTrue(effect.isInfiniteDuration()&&effect.getAmplifier()==0,"Indicator lasts while attuned without a misleading countdown or level");
            TrophyEffects.changed(player);
            h.assertTrue(player.getEffect(TrophyAttunement.EFFECTS.get(kind))==effect,"Unchanged selection does not reapply its status every tick");
        }
        h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_indicator_survives_milk_and_clears_with_attunement(GameTestHelper h){
        var player=player(h);select(player,TrophyKind.ZOMBIE);TrophyEffects.changed(player);
        var indicator=TrophyAttunement.EFFECTS.get(TrophyKind.ZOMBIE);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,400));
        Content.MILK.get().finishUsingItem(new ItemStack(Content.MILK.get()),h.getLevel(),player);
        h.assertFalse(player.hasEffect(net.minecraft.world.effect.MobEffects.POISON),"Vial still removes a harmful effect");
        h.assertTrue(player.hasEffect(indicator),"Vial preserves the active trophy indicator");
        Items.MILK_BUCKET.finishUsingItem(new ItemStack(Items.MILK_BUCKET),h.getLevel(),player);
        h.assertTrue(player.hasEffect(indicator)&&TrophyEffects.active(player)==TrophyKind.ZOMBIE,"Milk bucket cannot hide unchanged attunement");
        player.removeEffect(indicator);player.tickCount=20;
        TrophyEffects.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        h.assertTrue(player.hasEffect(indicator),"Server reconciliation restores a missing indicator");
        TrophyLedger.get(h.getLevel()).clear(player.getUUID());TrophyEffects.changed(player);
        h.assertTrue(TrophyAttunement.EFFECTS.values().stream().noneMatch(player::hasEffect),"Ending attunement clears all trophy indicators");
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(indicator,-1));
        h.assertTrue(TrophyEffects.active(player)==null,"Applying an indicator alone grants no trophy attunement");
        TrophyEffects.changed(player);
        h.assertFalse(player.hasEffect(indicator),"Unselected indicators are removed");h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_shape_matches_supplied_stand_and_head(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(2,2,2));
        for(var kind:TrophyKind.values()){
            var state=Trophies.BLOCKS.get(kind).get().defaultBlockState();
            var shape=state.getShape(h.getLevel(),pos);
            var expected=net.minecraft.world.phys.shapes.Shapes.or(
                net.minecraft.world.level.block.Block.box(3,0,3,13,2,13),
                net.minecraft.world.level.block.Block.box(6,2,6,10,3,10),
                net.minecraft.world.level.block.Block.box(7,3,7,9,6,9),
                net.minecraft.world.level.block.Block.box(4,6,4,12,14,12));
            h.assertFalse(net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(shape,expected,net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME),"Every trophy uses the supplied model's four-piece interaction shape");
        }
        h.succeed();
    }
    private static InteractionResult placeTrophy(GameTestHelper h,Player player,TrophyKind kind,BlockPos pos){
        var floor=pos.below();h.getLevel().setBlockAndUpdate(floor,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        var stack=new ItemStack(Trophies.BLOCKS.get(kind).get());
        return ((BlockItem)stack.getItem()).place(new BlockPlaceContext(player,InteractionHand.MAIN_HAND,stack,new BlockHitResult(Vec3.atCenterOf(floor),Direction.UP,floor,false)));
    }
    @GameTest(template="empty")
    public static void trophy_placement_limit_is_per_owner_and_type_and_resets_on_break(GameTestHelper h){
        var owner=player(h);var other=player(h);var first=h.absolutePos(new BlockPos(2,2,2));var second=h.absolutePos(new BlockPos(4,2,2));var third=h.absolutePos(new BlockPos(6,2,2));var replacement=h.absolutePos(new BlockPos(2,2,4));
        h.assertTrue(placeTrophy(h,owner,TrophyKind.CREEPER,first).consumesAction(),"First owned trophy can be placed");
        h.assertTrue(placeTrophy(h,owner,TrophyKind.CREEPER,replacement)==InteractionResult.FAIL,"A second standing trophy of the same type is rejected");
        h.assertTrue(placeTrophy(h,owner,TrophyKind.SKELETON,second).consumesAction(),"The owner may place a different trophy type");
        h.assertTrue(placeTrophy(h,other,TrophyKind.CREEPER,third).consumesAction(),"Another player has an independent placement slot");
        TrophyLedger.get(h.getLevel()).attune(owner.getUUID(),TrophyKind.SKELETON);
        h.getLevel().destroyBlock(first,true,other);
        var ledger=TrophyLedger.get(h.getLevel());
        h.assertFalse(ledger.hasPlaced(owner.getUUID(),TrophyKind.CREEPER),"Breaking someone else's trophy releases its owner's slot");
        h.assertTrue(ledger.hasPlaced(other.getUUID(),TrophyKind.CREEPER)&&ledger.hasPlaced(owner.getUUID(),TrophyKind.SKELETON),"Other owners and types keep their standing reservations");
        h.assertTrue(ledger.active(owner.getUUID())==TrophyKind.SKELETON,"Destroying an inactive type does not clear the selected trophy");
        h.assertTrue(placeTrophy(h,owner,TrophyKind.CREEPER,replacement).consumesAction(),"A replacement may be placed at a new position");
        h.assertTrue(placeTrophy(h,owner,TrophyKind.CREEPER,first)==InteractionResult.FAIL,"The replacement re-establishes the one-at-a-time limit");h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_placement_reservations_save_and_release_independently(GameTestHelper h){
        var ledger=new TrophyLedger();var owner=UUID.randomUUID();
        ledger.recordPlacement(owner,TrophyKind.CREEPER);ledger.recordPlacement(owner,TrophyKind.SKELETON);ledger.attune(owner,TrophyKind.SKELETON);
        ledger=TrophyLedger.load(ledger.save(new net.minecraft.nbt.CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(ledger.hasPlaced(owner,TrophyKind.CREEPER)&&ledger.hasPlaced(owner,TrophyKind.SKELETON),"Saving preserves slots for standing trophies");
        h.assertTrue(ledger.releasePlacement(owner,TrophyKind.CREEPER),"Removal releases exactly the specified type");
        ledger=TrophyLedger.load(ledger.save(new net.minecraft.nbt.CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertFalse(ledger.hasPlaced(owner,TrophyKind.CREEPER),"Released slots remain free after reload");
        h.assertTrue(ledger.hasPlaced(owner,TrophyKind.SKELETON)&&ledger.active(owner)==TrophyKind.SKELETON,"Another type and its attunement survive reload");
        h.assertTrue(ledger.recordPlacement(owner,TrophyKind.CREEPER),"Released type can be reserved again");
        h.assertFalse(ledger.recordPlacement(owner,TrophyKind.CREEPER),"A standing type cannot reserve another slot");h.succeed();
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
        var player=player(h);select(player,TrophyKind.ZOMBIE);TrophyEffects.changed(player);TrophyEffects.clone(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(player,player,true));
        h.assertTrue(TrophyEffects.active(player)==null,"Death clears active trophy");h.assertTrue(TrophyAttunement.EFFECTS.values().stream().noneMatch(player::hasEffect),"Death clears trophy status indicators");h.assertTrue(TrophyLedger.get(h.getLevel()).hasPlaced(player.getUUID(),TrophyKind.ZOMBIE),"Death preserves the slot for the trophy still standing");h.succeed();
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
