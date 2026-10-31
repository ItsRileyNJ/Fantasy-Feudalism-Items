package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.*;
import com.fantasyfeudalism.items.lighting.*;
import com.fantasyfeudalism.items.mount.*;
import com.fantasyfeudalism.items.spawner.SpawnerEyes;
import com.fantasyfeudalism.items.trophy.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class ContentTests {
    private static Player player(GameTestHelper h){return h.makeMockPlayer(GameType.SURVIVAL);}
    private static void cauldronClick(GameTestHelper h,Player player,BlockPos pos){
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        MilkCauldron.interact(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,hit));
    }
    @GameTest(template="empty")
    public static void milk_bucket_and_three_vials(GameTestHelper h){
        var player=player(h);var pos=h.absolutePos(new BlockPos(2,2,2));
        h.getLevel().setBlockAndUpdate(pos,Blocks.CAULDRON.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MILK_BUCKET));
        cauldronClick(h,player,pos);h.assertTrue(h.getLevel().getBlockState(pos).is(Content.MILK_CAULDRON.get()),"Bucket fills a milk cauldron");h.assertTrue(player.getMainHandItem().is(Items.BUCKET),"Milk bucket returns empty bucket");
        for(int i=0;i<3;i++){player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLASS_BOTTLE));cauldronClick(h,player,pos);h.assertTrue(player.getMainHandItem().is(Content.MILK.get()),"Each of three bottles fills with milk");}
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.CAULDRON),"Three vials empty the cauldron");h.succeed();
    }
    @GameTest(template="empty")
    public static void milk_halves_effects_and_restores_fractional_food(GameTestHelper h){
        var player=player(h);player.getFoodData().setFoodLevel(10);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0));player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,1));player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,200,2));player.addEffect(new MobEffectInstance(MobEffects.JUMP,200,3));
        var result=Content.MILK.get().finishUsingItem(new ItemStack(Content.MILK.get()),h.getLevel(),player);
        h.assertTrue(result.is(Items.GLASS_BOTTLE),"Drinking returns a glass bottle");
        h.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED),"Level I is removed");
        h.assertTrue(player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier()==0&&player.getEffect(MobEffects.DAMAGE_BOOST).getDuration()==100,"II becomes I and duration halves");
        h.assertTrue(player.getEffect(MobEffects.DIG_SPEED).getAmplifier()==0,"III rounds down to I");h.assertTrue(player.getEffect(MobEffects.JUMP).getAmplifier()==1,"IV becomes II");
        h.assertTrue(player.getFoodData().getFoodLevel()==12,"First vial grants two whole food points");h.assertTrue(player.getPersistentData().getDouble("ff_milk_food_fraction")==.5,"Half point is retained");
        Content.MILK.get().finishUsingItem(new ItemStack(Content.MILK.get()),h.getLevel(),player);h.assertTrue(player.getFoodData().getFoodLevel()==15,"Two vials restore exactly five points");
        h.assertFalse(player.hasEffect(MobEffects.DAMAGE_BOOST),"Second vial removes remaining level-I effect");h.succeed();
    }
    @GameTest(template="empty")
    public static void animal_brewing_has_only_drinkable_forms(GameTestHelper h){
        var brewer=h.getLevel().getServer().potionBrewing();var item=new ItemStack(Content.MILK.get());
        Item[] ingredients={Items.NETHER_WART,Items.HONEYCOMB,Items.RABBIT_FOOT,Items.REDSTONE};
        Item[] outputs={AnimalAllegiance.AWKWARD.get(),AnimalAllegiance.INTERMEDIATE.get(),AnimalAllegiance.NORMAL.get(),AnimalAllegiance.EXTENDED.get()};
        for(int i=0;i<ingredients.length;i++){h.assertTrue(brewer.hasMix(item,new ItemStack(ingredients[i])),"Milk brewing stage is valid");item=brewer.mix(new ItemStack(ingredients[i]),item);h.assertTrue(item.is(outputs[i]),"Milk brewing produces correct stage");h.assertFalse(brewer.hasMix(item,new ItemStack(Items.GUNPOWDER)),"Milk potion has no splash form");h.assertFalse(brewer.hasMix(item,new ItemStack(Items.DRAGON_BREATH)),"Milk potion has no lingering form");}
        var contents=new ItemStack(AnimalAllegiance.NORMAL.get()).get(DataComponents.POTION_CONTENTS);var effects=new ArrayList<MobEffectInstance>();contents.forEachEffect(effects::add);h.assertTrue(effects.size()==1&&effects.getFirst().getDuration()==1200,"Normal allegiance lasts one minute");
        effects.clear();item.get(DataComponents.POTION_CONTENTS).forEachEffect(effects::add);h.assertTrue(effects.getFirst().getDuration()==3600,"Extended allegiance lasts three minutes");h.succeed();
    }
    @GameTest(template="empty")
    public static void allegiance_excludes_neutral_and_owned_animals(GameTestHelper h){
        var cow=h.spawnWithNoFreeWill(EntityType.COW,2,2,2);var wolf=EntityType.WOLF.create(h.getLevel());var horse=EntityType.HORSE.create(h.getLevel());
        h.assertTrue(AnimalAllegiance.eligible(cow),"Passive cow is eligible");h.assertFalse(AnimalAllegiance.eligible(wolf),"Neutral wolves excluded");
        horse.setOwnerUUID(UUID.randomUUID());h.assertFalse(AnimalAllegiance.eligible(horse),"Player-owned mounts excluded");h.assertFalse(AnimalAllegiance.eligible(EntityType.GOAT.create(h.getLevel())),"Neutral goats excluded");h.succeed();
    }
    @GameTest(template="empty")
    public static void saddlebag_storage_serialization_and_empty_nested_containers(GameTestHelper h){
        var bag=new ItemStack(MountContent.SADDLEBAG.get());var contents=NonNullList.withSize(27,ItemStack.EMPTY);contents.set(26,new ItemStack(Items.DIAMOND,7));bag.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents));
        var restored=ItemStack.parse(h.getLevel().registryAccess(),bag.save(h.getLevel().registryAccess())).orElseThrow();
        h.assertTrue(restored.get(DataComponents.CONTAINER).getStackInSlot(26).getCount()==7,"Last of twenty-seven slots survives save/load");
        h.assertFalse(SaddlebagItem.allowed(restored),"Filled nested bags rejected");h.assertTrue(SaddlebagItem.allowed(new ItemStack(MountContent.SADDLEBAG.get())),"Empty nested bags allowed");
        var shulker=new ItemStack(Blocks.SHULKER_BOX);h.assertTrue(SaddlebagItem.allowed(shulker),"Empty shulker allowed");shulker.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.STONE))));h.assertFalse(SaddlebagItem.allowed(shulker),"Filled shulker rejected");
        var dyed=DyedItemColor.applyDyes(restored,List.of((DyeItem)Items.RED_DYE));h.assertTrue(dyed.get(DataComponents.CONTAINER).getStackInSlot(26).getCount()==7,"Dyeing preserves inventory");h.assertTrue(dyed.has(DataComponents.DYED_COLOR),"Dye color is stored");h.succeed();
    }
    @GameTest(template="empty")
    public static void saddlebag_menu_prevents_moving_open_bag(GameTestHelper h){
        var player=player(h);var bag=new ItemStack(MountContent.SADDLEBAG.get());player.getInventory().setItem(0,bag);var menu=new BagMenu(9,player.getInventory(),bag);
        h.assertTrue(menu.getContainer().getContainerSize()==27,"Bag menu has twenty-seven cargo slots");
        var root=menu.slots.stream().filter(slot->slot.getItem()==bag).findFirst().orElseThrow();h.assertFalse(root.mayPickup(player),"Open bag cannot be picked up into its own inventory");
        h.assertTrue(menu.quickMoveStack(player,root.index).isEmpty(),"Shift-click cannot move open bag");
        menu.getContainer().setItem(0,new ItemStack(Items.EMERALD,3));menu.getContainer().setChanged();h.assertTrue(bag.get(DataComponents.CONTAINER).getStackInSlot(0).getCount()==3,"Menu writes directly back to its bag item");h.succeed();
    }
    @GameTest(template="empty")
    public static void horseshoes_stack_and_remove_immediately(GameTestHelper h){
        var horse=h.spawnWithNoFreeWill(EntityType.HORSE,2,2,2);double speed=horse.getAttributeValue(Attributes.MOVEMENT_SPEED),jump=horse.getAttributeValue(Attributes.JUMP_STRENGTH);
        horse.jumpFromGround();double normalVelocity=horse.getDeltaMovement().y;
        MountEquipment.set(horse,2,new ItemStack(MountContent.GOLD.get()));MountEquipment.apply(horse);horse.jumpFromGround();
        h.assertTrue(Math.abs(horse.getAttributeValue(Attributes.MOVEMENT_SPEED)-speed*1.11)<.00001,"Gold adds eleven percent to natural speed");
        h.assertTrue(Math.abs(MountEquipment.jumpHeight(horse.getDeltaMovement().y)/MountEquipment.jumpHeight(normalVelocity)-1.1)<.00001,"Gold adds ten percent actual jump height");
        MountEquipment.set(horse,2,ItemStack.EMPTY);MountEquipment.apply(horse);h.assertTrue(Math.abs(horse.getAttributeValue(Attributes.MOVEMENT_SPEED)-speed)<.00001,"Removing shoe removes speed bonus");h.assertTrue(horse.getAttributeValue(Attributes.JUMP_STRENGTH)==jump,"Removing shoe removes jump bonus");
        h.assertFalse(new ItemStack(MountContent.NETHERITE.get()).isDamageableItem(),"Horseshoes never wear out");h.succeed();
    }
    @GameTest(template="empty")
    public static void mount_slots_and_filled_bag_removal(GameTestHelper h){
        var donkey=h.spawnWithNoFreeWill(EntityType.DONKEY,2,2,2);var inventory=new MountInventory(donkey);var bag=new ItemStack(MountContent.SADDLEBAG.get());
        inventory.setItem(0,new ItemStack(Items.SADDLE));inventory.setItem(3,new ItemStack(Items.LANTERN));inventory.setItem(4,bag);inventory.setItem(9,new ItemStack(Items.GOLD_INGOT,13));inventory.setChanged();
        h.assertTrue(donkey.isSaddled(),"Saddle remains independent");h.assertTrue(MountEquipment.item(donkey,3).is(Items.LANTERN),"Lantern remains independent");
        var removed=inventory.removeItem(4,1);inventory.setChanged();h.assertTrue(removed.get(DataComponents.CONTAINER).getStackInSlot(0).getCount()==13,"Removed bag preserves entire cargo");
        h.assertTrue(inventory.getItem(9).isEmpty(),"Removing bag removes access to its cargo");h.assertTrue(inventory.getItem(0).is(Items.SADDLE)&&inventory.getItem(3).is(Items.LANTERN),"Bag removal leaves other equipment unchanged");
        h.assertFalse(inventory.canPlaceItem(2,new ItemStack(MountContent.IRON.get())),"Donkeys do not get horseshoe slot");h.succeed();
    }
    @GameTest(template="empty")
    public static void two_item_netherite_smithing(GameTestHelper h){
        var input=new SmithingRecipeInput(ItemStack.EMPTY,new ItemStack(MountContent.DIAMOND.get()),new ItemStack(Items.NETHERITE_INGOT));
        var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,h.getLevel()).orElseThrow();
        h.assertTrue(recipe.value().assemble(input,h.getLevel().registryAccess()).is(MountContent.NETHERITE.get()),"Diamond plus ingot upgrades without template");
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        try{var serializer=new NetheriteHorseshoeRecipe.Serializer();serializer.streamCodec().encode(buffer,new NetheriteHorseshoeRecipe());h.assertTrue(serializer.streamCodec().decode(buffer).matches(input,h.getLevel()),"Smithing recipe synchronizes over the network without object-identity dependence");}finally{buffer.release();}h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_recipes_require_sigil(GameTestHelper h){
        var sigil=h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Trophies.SIGIL);
        for(var kind:TrophyKind.values()){
            var head=new ItemStack(Trophies.headItem(kind));var input=CraftingInput.of(2,1,List.of(head,new ItemStack(Content.TROPHY_MOUNT.get())));
            h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).isEmpty(),"Normal head cannot craft a trophy");
            head.enchant(sigil,1);var enchanted=CraftingInput.of(2,1,List.of(head,new ItemStack(Content.TROPHY_MOUNT.get())));
            var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,enchanted,h.getLevel()).orElseThrow();
            h.assertTrue(recipe.value().assemble(enchanted,h.getLevel().registryAccess()).is(Trophies.BLOCKS.get(kind).get().asItem()),"Sigil head produces matching trophy");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void sigil_level30_enchanting_costs(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(3,2,3));h.getLevel().setBlockAndUpdate(pos,Blocks.ENCHANTING_TABLE.defaultBlockState());
        for(var offset:EnchantingTableBlock.BOOKSHELF_OFFSETS)h.getLevel().setBlockAndUpdate(pos.offset(offset),Blocks.BOOKSHELF.defaultBlockState());
        var player=player(h);player.experienceLevel=30;var menu=new EnchantmentMenu(10,player.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));
        menu.getSlot(0).set(new ItemStack(Items.ZOMBIE_HEAD));menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI,3));menu.slotsChanged(menu.getSlot(0).container);
        h.assertTrue(menu.costs[0]==0&&menu.costs[1]==0&&menu.costs[2]==30,"Only the level-30 option is available");h.assertFalse(menu.clickMenuButton(player,0),"Lower options rejected");h.assertTrue(menu.clickMenuButton(player,2),"Level-30 option enchants head");
        h.assertTrue(menu.getSlot(0).getItem().get(DataComponents.ENCHANTMENTS).entrySet().stream().allMatch(entry->entry.getKey().is(Trophies.SIGIL)),"Only Sigil is applied");
        h.assertTrue(player.experienceLevel==27,"Normal three-level XP cost");h.assertTrue(menu.getSlot(1).getItem().isEmpty(),"Normal three-lapis cost");h.succeed();
    }
    @GameTest(template="empty")
    public static void trophy_history_serialization_and_single_selection(GameTestHelper h){
        var owner=UUID.randomUUID();var data=new TrophyLedger();h.assertTrue(data.recordPlacement(owner,TrophyKind.ZOMBIE),"First placement succeeds");h.assertFalse(data.recordPlacement(owner,TrophyKind.ZOMBIE),"Duplicate placement rejected");
        data.attune(owner,TrophyKind.ZOMBIE);data.attune(owner,TrophyKind.SKELETON);h.assertTrue(data.active(owner)==TrophyKind.SKELETON,"Second attunement replaces first");
        var restored=TrophyLedger.load(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());h.assertTrue(restored.active(owner)==TrophyKind.SKELETON&&restored.hasPlaced(owner,TrophyKind.ZOMBIE),"Attunement and permanent history survive restart serialization");
        restored.clear(owner);h.assertTrue(restored.active(owner)==null&&restored.hasPlaced(owner,TrophyKind.ZOMBIE),"Clearing on death does not permit another placement");h.succeed();
    }
    @GameTest(template="empty")
    public static void fishing_roll_order_and_capacity(GameTestHelper h){
        var queue=new ArrayDeque<Double>(List.of(.1));h.assertTrue(FishingTrapEntity.roll(queue::remove)==FishingTrapEntity.CatchType.FISH&&queue.isEmpty(),"Fish success never rolls junk");
        queue=new ArrayDeque<>(List.of(.5,.1,.79));h.assertTrue(FishingTrapEntity.roll(queue::remove)==FishingTrapEntity.CatchType.JUNK,"Failed fish then successful catch selects eighty-percent junk");
        queue=new ArrayDeque<>(List.of(.5,.1,.8));h.assertTrue(FishingTrapEntity.roll(queue::remove)==FishingTrapEntity.CatchType.TREASURE,"Twenty-percent treasure boundary");
        queue=new ArrayDeque<>(List.of(.5,.5));h.assertTrue(FishingTrapEntity.roll(queue::remove)==FishingTrapEntity.CatchType.NOTHING,"Both failed rolls catch nothing");
        var trap=new FishingTrapEntity(h.absolutePos(BlockPos.ZERO),Content.FISHING_TRAP.get().defaultBlockState());trap.setLevel(h.getLevel());
        var list=new ListTag();for(int i=0;i<8;i++)list.add(new ItemStack(Items.SALMON).save(h.getLevel().registryAccess()));var nbt=new CompoundTag();nbt.put("Catches",list);nbt.putInt("Elapsed",1234);trap.loadCustomOnly(nbt,h.getLevel().registryAccess());
        trap.attempt(h.getLevel());h.assertTrue(trap.catches().size()==8&&trap.elapsed()==1234,"Full trap does not exceed eight shared catches");
        var restored=new FishingTrapEntity(trap.getBlockPos(),trap.getBlockState());restored.loadCustomOnly(trap.saveCustomOnly(h.getLevel().registryAccess()),h.getLevel().registryAccess());h.assertTrue(restored.catches().size()==8&&restored.elapsed()==1234,"Catch inventory and timer survive restart");
        var player=player(h);trap.collect(player);h.assertTrue(trap.catches().isEmpty()&&player.getInventory().countItem(Items.SALMON)==8,"Right-click collection takes all catches");h.succeed();
    }
    @GameTest(template="empty")
    public static void spawner_eye_matches_mob_and_chooses_nearest(GameTestHelper h){
        var origin=h.absolutePos(new BlockPos(1,2,1));var near=origin.offset(2,0,0);var far=origin.offset(4,0,0);
        for(var pos:List.of(near,far)){h.getLevel().setBlockAndUpdate(pos,Blocks.SPAWNER.defaultBlockState());((SpawnerBlockEntity)h.getLevel().getBlockEntity(pos)).setEntityId(EntityType.ZOMBIE,h.getLevel().random);}
        h.assertTrue(SpawnerEyes.find(h.getLevel(),origin,"zombie").orElseThrow().equals(near),"Nearest matching spawner found");h.assertTrue(SpawnerEyes.find(h.getLevel(),origin,"blaze").isEmpty(),"Different mob type excluded");h.succeed();
    }
    @GameTest(template="empty")
    public static void dagger_stats_and_armor_only_penetration(GameTestHelper h){
        var player=player(h);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Content.BONE_DAGGER.get()));
        var victim=h.spawnWithNoFreeWill(EntityType.COW,3,2,3);victim.getAttribute(Attributes.ARMOR).setBaseValue(20);
        float before=victim.getHealth();victim.hurt(h.getLevel().damageSources().playerAttack(player),10);
        h.assertTrue(Math.abs((before-victim.getHealth())-4.88)<.001,"Twenty percent bypasses armor while eighty percent uses normal armor calculation");
        var dagger=new ItemStack(Content.BONE_DAGGER.get());h.assertTrue(dagger.getMaxDamage()==100,"Dagger has one hundred durability");h.assertTrue(Content.BONE_DAGGER.get().isValidRepairItem(dagger,new ItemStack(Items.FLINT)),"Dagger repairs with flint");
        h.assertFalse(EntityType.ZOMBIE.create(h.getLevel()).canHoldItem(dagger),"Mobs cannot pick up dagger");
        var protection=h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
        var chest=new ItemStack(Items.DIAMOND_CHESTPLATE);chest.enchant(protection,4);victim.setItemSlot(EquipmentSlot.CHEST,chest);victim.setHealth(20);victim.invulnerableTime=0;
        float protectedBefore=victim.getHealth();victim.hurt(h.getLevel().damageSources().playerAttack(player),10);h.assertTrue(protectedBefore-victim.getHealth()<4.88,"Protection still reduces armor-penetrating damage");h.succeed();
    }
    @GameTest(template="empty")
    public static void dagger_accepts_exactly_sword_enchantments(GameTestHelper h){
        var dagger=new ItemStack(Content.BONE_DAGGER.get());var sword=new ItemStack(Items.IRON_SWORD);
        for(var enchantment:h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT))h.assertTrue(enchantment.isSupportedItem(dagger)==enchantment.isSupportedItem(sword),"Dagger enchantment pool must match a vanilla sword: "+enchantment.description().getString());
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void fuel_exhaustion_and_soul_immunity(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(2,2,2));h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.LANTERN.defaultBlockState());
        LightFuel.setRemaining(h.getLevel(),pos,1);
        h.succeedWhen(()->{h.assertFalse(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT),"Fuel exhaustion switches off lantern");h.assertTrue(LightFuel.remaining(h.getLevel(),pos)==0,"Fuel stops at zero");});
    }
    @GameTest(template="empty")
    public static void light_item_fuel_and_refill_rules(GameTestHelper h){
        var lantern=new ItemStack(Items.LANTERN);LightItemFuel.set(lantern,100,false);MountEquipment.refill(lantern);h.assertFalse(MountEquipment.lit(lantern),"Refilling nonempty switched-off lantern preserves off state");
        LightItemFuel.set(lantern,0,false);MountEquipment.refill(lantern);h.assertTrue(MountEquipment.lit(lantern),"Refilling an empty lantern switches it on");
        var restored=ItemStack.parse(h.getLevel().registryAccess(),lantern.save(h.getLevel().registryAccess())).orElseThrow();h.assertTrue(LightItemFuel.remaining(restored)==LightFuel.capacity(Blocks.LANTERN.defaultBlockState()),"Full fuel persists on item");h.assertTrue(LightItemFuel.remaining(new ItemStack(Items.SOUL_LANTERN))==Long.MAX_VALUE,"Soul lights have no finite fuel budget");h.succeed();
    }
    @GameTest(template="empty")
    public static void soul_light_protection_respects_radius_and_walls(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,2,1));h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.SOUL_TORCH.defaultBlockState());
        var zombie=h.spawnWithNoFreeWill(EntityType.ZOMBIE,4,2,1);h.assertTrue(LightFuel.protectedFromSun(zombie),"Nearby soul torch protects sunlight-vulnerable mob");
        h.getLevel().setBlockAndUpdate(pos.offset(2,0,0),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos.offset(2,1,0),Blocks.STONE.defaultBlockState());h.assertFalse(LightFuel.protectedFromSun(zombie),"Solid wall blocks soul protection");
        zombie.setPos(Vec3.atCenterOf(pos.offset(8,0,0)));h.assertFalse(LightFuel.protectedFromSun(zombie),"Soul torch range is only six blocks");h.succeed();
    }
    @GameTest(template="empty")
    public static void mounted_fuel_uses_world_clock_not_mount_age(GameTestHelper h){
        var horse=EntityType.HORSE.create(h.getLevel());horse.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(2,2,2))));horse.tickCount=7;
        var lantern=new ItemStack(Items.LANTERN);LightItemFuel.set(lantern,100,true);MountEquipment.set(horse,3,lantern);
        h.succeedWhen(()->{
            h.assertTrue(h.getLevel().getGameTime()%20==0,"Wait for the fuel clock boundary");
            MountEquipment.tick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(horse));
            h.assertTrue(LightItemFuel.remaining(MountEquipment.item(horse,3))==80,"Mounted lantern burns fuel regardless of the mount's spawn tick");
        });
    }

    @GameTest(template="empty")
    public static void light_render_reads_and_hand_updates_preserve_item_identity(GameTestHelper h){
        var lamp=new ItemStack(Items.LANTERN);var changed=lamp.copy();LightItemFuel.set(changed,80,true);
        h.assertTrue(LightItemFuel.sameHeldLight(lamp,changed),"Burning fuel must not replay hand equip animation");
        LightItemFuel.set(changed,0,false);h.assertTrue(LightItemFuel.sameHeldLight(lamp,changed),"Light switches do not put away the held item");
        changed.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Named lantern"));
        h.assertFalse(LightItemFuel.sameHeldLight(lamp,changed),"Other item changes remain visible to the equip hook");
        h.assertFalse(LightItemFuel.sameHeldLight(lamp,new ItemStack(Items.SOUL_LANTERN)),"Switching lantern type still equips");
        var data=lamp.getComponents();for(int i=0;i<10000;i++)h.assertTrue(LightItemFuel.lit(lamp),"Fresh lantern remains lit");
        h.assertTrue(lamp.getComponents()==data,"Render reads never replace item data");h.succeed();
    }
    @GameTest(template="empty")
    public static void mount_visuals_sync_only_when_appearance_changes(GameTestHelper h){
        var horse=EntityType.HORSE.create(h.getLevel());var lamp=new ItemStack(Items.LANTERN);LightItemFuel.set(lamp,100,true);MountEquipment.set(horse,3,lamp);
        var data=horse.getData(com.fantasyfeudalism.items.content.ModAttachments.MOUNT);var decoded=MountVisuals.lantern(horse);
        LightItemFuel.set(lamp,80,true);MountEquipment.set(horse,3,lamp);
        h.assertTrue(data==horse.getData(com.fantasyfeudalism.items.content.ModAttachments.MOUNT),"Timer-only updates do not resend mount visuals");
        h.assertTrue(decoded==MountVisuals.lantern(horse),"Repeated frames reuse decoded items");
        h.assertTrue(LightItemFuel.remaining(MountEquipment.item(horse,3))==80,"Server still saves exact fuel");
        LightItemFuel.set(lamp,0,false);MountEquipment.set(horse,3,lamp);
        h.assertTrue(data!=horse.getData(com.fantasyfeudalism.items.content.ModAttachments.MOUNT),"Light-off transition is synchronized");
        h.assertFalse(LightItemFuel.lit(MountVisuals.lantern(horse)),"Cached light updates after attachment replacement");h.succeed();
    }

}
