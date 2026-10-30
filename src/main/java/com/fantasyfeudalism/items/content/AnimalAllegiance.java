package com.fantasyfeudalism.items.content;

import com.fantasyfeudalism.items.FeudalItems;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.bus.api.IEventBus;

public final class AnimalAllegiance {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, FeudalItems.ID);
    public static final DeferredHolder<MobEffect, MobEffect> EFFECT = EFFECTS.register("animal_allegiance", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xE3BD74) {});
    public static final DeferredItem<Item> AWKWARD = Content.ITEMS.register("awkward_milk_potion", () -> new Drink(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> INTERMEDIATE = Content.ITEMS.register("intermediate_milk_potion", () -> new Drink(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> NORMAL = Content.ITEMS.register("animal_allegiance_potion", () -> new Drink(new Item.Properties().stacksTo(1).component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(EFFECT, 1200))))));
    public static final DeferredItem<Item> EXTENDED = Content.ITEMS.register("extended_animal_allegiance_potion", () -> new Drink(new Item.Properties().stacksTo(1).component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(EFFECT, 3600))))));
    private static final String CLAIM = "fantasy_feudalism_allegiance_owner";
    public static final Set<EntityType<?>> PASSIVE = Set.of(EntityType.COW, EntityType.MOOSHROOM, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.RABBIT,
        EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.CAMEL, EntityType.CAT, EntityType.OCELOT, EntityType.TURTLE,
        EntityType.SNIFFER, EntityType.ARMADILLO, EntityType.BAT, EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.SQUID,
        EntityType.GLOW_SQUID, EntityType.TADPOLE, EntityType.FROG);
    public static final class Drink extends PotionItem {
        Drink(Item.Properties properties) { super(properties); }
        @Override public String getDescriptionId(ItemStack stack) { return getDescriptionId(); }
    }
    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
        // Force registration of the drinkable-only item forms before item registry events.
    }
    public static boolean eligible(Mob mob) {
        return PASSIVE.contains(mob.getType()) && !(mob instanceof NeutralMob)
            && (!(mob instanceof OwnableEntity owned) || owned.getOwnerUUID() == null);
    }
    public static boolean claimed(Mob mob) {
        var data=mob.getPersistentData();if(!data.hasUUID(CLAIM))return false;
        if(!eligible(mob)){data.remove(CLAIM);return false;}
        if(mob.getServer()==null)return true;
        var player=mob.getServer().getPlayerList().getPlayer(data.getUUID(CLAIM));
        // Offline potion timers pause normally; retain the first player's claim until
        // that player returns and the actual effect state can be checked.
        if(player!=null&&(!player.isAlive()||!player.hasEffect(EFFECT))){data.remove(CLAIM);return false;}
        return true;
    }
    public static net.minecraft.server.level.ServerPlayer owner(Mob mob) {
        if(!claimed(mob)||mob.getServer()==null)return null;
        var player=mob.getServer().getPlayerList().getPlayer(mob.getPersistentData().getUUID(CLAIM));
        return player!=null&&player.level()==mob.level()?player:null;
    }
    public static boolean claim(Mob mob,net.minecraft.server.level.ServerPlayer player){
        if(!eligible(mob)||claimed(mob)||player.level()!=mob.level()||!player.hasEffect(EFFECT)||!player.isAlive()||mob.distanceToSqr(player)>1024)return false;
        mob.getPersistentData().putUUID(CLAIM,player.getUUID());return true;
    }
    public static void attract(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) || player.tickCount % 10 != 0 || !player.hasEffect(EFFECT)) return;
        for (var mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32), mob -> eligible(mob) && mob.distanceToSqr(player) <= 1024)) {
            claim(mob,player);
        }
    }
    public static void join(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof Mob mob) || !PASSIVE.contains(mob.getType())) return;
        mob.goalSelector.addGoal(-1, new Follow(mob));
    }
    private static final class Follow extends Goal {
        private final Mob mob;
        Follow(Mob mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)); }
        @Override public boolean canUse() { return owner(mob) != null; }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            var player = owner(mob); if (player == null) return;
            mob.getLookControl().setLookAt(player, 30, 30);
            if (mob.distanceToSqr(player) > 4) mob.getNavigation().moveTo(player, 1.0); else mob.getNavigation().stop();
        }
        @Override public void stop() { mob.getNavigation().stop(); claimed(mob); }
    }
    public static void brewing(RegisterBrewingRecipesEvent event) {
        add(event, Content.MILK.get(), Items.NETHER_WART, AWKWARD.get());
        add(event, AWKWARD.get(), Items.HONEYCOMB, INTERMEDIATE.get());
        add(event, INTERMEDIATE.get(), Items.RABBIT_FOOT, NORMAL.get());
        add(event, NORMAL.get(), Items.REDSTONE, EXTENDED.get());
    }
    private static void add(RegisterBrewingRecipesEvent event, Item input, Item ingredient, Item output) {
        event.getBuilder().addRecipe(new IBrewingRecipe() {
            public boolean isInput(ItemStack stack) { return stack.is(input); }
            public boolean isIngredient(ItemStack stack) { return stack.is(ingredient); }
            public ItemStack getOutput(ItemStack in, ItemStack added) { return isInput(in) && isIngredient(added) ? new ItemStack(output) : ItemStack.EMPTY; }
        });
    }
    private AnimalAllegiance() {}
}
