package com.fantasyfeudalism.items;

import com.fantasyfeudalism.items.clairvoyance.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.*;

@Mod(FeudalItems.ID)
public final class FeudalItems {
    public static final String ID = "fantasy_feudalism_items";
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredHolder<MobEffect, MobEffect> CLAIRVOYANCE = EFFECTS.register("clairvoyance",
        () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xB3A1E6) {});
    public static final DeferredHolder<Potion, Potion> CLAIRVOYANCE_POTION = POTIONS.register("clairvoyance", ClairvoyancePotion::new);

    static {
        TABS.register("items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ID))
            .icon(() -> potionStack(Items.POTION))
            .displayItems((parameters, output) -> {
                output.accept(potionStack(Items.POTION));
                output.accept(potionStack(Items.SPLASH_POTION));
                output.accept(potionStack(Items.LINGERING_POTION));
                output.accept(potionStack(Items.TIPPED_ARROW));
                com.fantasyfeudalism.items.content.Content.creative(output::accept);
                output.accept(com.fantasyfeudalism.items.content.AnimalAllegiance.NORMAL.get());
                output.accept(com.fantasyfeudalism.items.content.AnimalAllegiance.EXTENDED.get());
            }).build());
    }

    public static ItemStack potionStack(Item item) {
        var stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(CLAIRVOYANCE_POTION));
        return stack;
    }

    public FeudalItems(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ClairvoyanceConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, com.fantasyfeudalism.items.lighting.WeatherLighting.SPEC, "fantasy_feudalism_items-weather.toml");
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.lighting.WeatherLighting::chunkLoaded);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.lighting.LightInteractions::interact);
        com.fantasyfeudalism.items.spawner.SpawnerEyes.init();
        com.fantasyfeudalism.items.mount.MountContent.init();
        com.fantasyfeudalism.items.mount.MountContent.MENUS.register(bus);
        com.fantasyfeudalism.items.mount.SaddlebagLoot.REGISTRY.register(bus);
        com.fantasyfeudalism.items.mount.MountRecipes.init();
        com.fantasyfeudalism.items.mount.SaddlebagDyeRecipe.init();
        com.fantasyfeudalism.items.content.ModAttachments.TYPES.register(bus);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.mount.MountEquipment::interact);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.mount.MountEquipment::drops);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.mount.MountEquipment::damage);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.mount.MountEquipment::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.mount.MountEquipment::join);
        com.fantasyfeudalism.items.trophy.Trophies.init();
        com.fantasyfeudalism.items.trophy.TrophyRecipes.REGISTRY.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, com.fantasyfeudalism.items.trophy.HeadDrops.SPEC, "fantasy_feudalism_items-head-drops.toml");
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.HeadDrops::drops);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.TrophyEffects::damage);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.TrophyEffects::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.TrophyEffects::xp);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.TrophyEffects::resistance);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.TrophyEffects::phantoms);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.TrophyEffects::clone);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.trophy.TrophyEffects::target);
        com.fantasyfeudalism.items.content.AnimalAllegiance.register(bus);
        com.fantasyfeudalism.items.content.Content.register(bus);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.content.AnimalAllegiance::brewing);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.content.AnimalAllegiance::attract);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.content.AnimalAllegiance::join);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.content.MilkCauldron::interact);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.content.MilkVial::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.content.VillagerTrades::register);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.compat.CaravanTrades::join);
        container.registerConfig(ModConfig.Type.SERVER, com.fantasyfeudalism.items.lighting.LightFuel.SPEC, "fantasy_feudalism_items-fuel.toml");
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.lighting.LightFuel::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.lighting.LightItemFuel::itemTick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.items.lighting.LightItemFuel::drops);
        EFFECTS.register(bus); POTIONS.register(bus); TABS.register(bus);
        bus.addListener(FeudalItems::network);
        NeoForge.EVENT_BUS.addListener(FeudalItems::brewing);
        NeoForge.EVENT_BUS.addListener(ClairvoyanceTracker::tick);
    }

    private static void brewing(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.THICK, Items.AMETHYST_SHARD, CLAIRVOYANCE_POTION);
    }

    private static void network(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(ClairvoyancePayload.TYPE, ClairvoyancePayload.CODEC,
            (payload, context) -> com.fantasyfeudalism.items.client.ClairvoyanceClient.receive(payload));
    }
}
