package com.fantasyfeudalism.items.trophy;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.Content;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class Trophies {
    public static final TagKey<Item> HEADS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(FeudalItems.ID, "sigil_heads"));
    public static final ResourceKey<Enchantment> SIGIL = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(FeudalItems.ID, "sigil"));
    public static final Map<TrophyKind, DeferredBlock<TrophyBlock>> BLOCKS = new EnumMap<>(TrophyKind.class);
    public static final DeferredItem<Item> STRAY_HEAD = Content.ITEMS.registerSimpleItem("stray_head");
    public static final DeferredItem<Item> BOGGED_HEAD = Content.ITEMS.registerSimpleItem("bogged_head");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrophyEntity>> BLOCK_ENTITY;
    static {
        for (var kind : TrophyKind.values()) {
            var block = Content.BLOCKS.register(kind.id() + "_trophy", () -> new TrophyBlock(kind, BlockBehaviour.Properties.of().strength(2).noOcclusion().noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
            BLOCKS.put(kind, block);
            Content.ITEMS.register(kind.id() + "_trophy", () -> new TrophyBlock.TrophyItem(block.get(), new Item.Properties().stacksTo(1)));
        }
        BLOCK_ENTITY = Content.BLOCK_ENTITIES.register("trophy", () -> BlockEntityType.Builder.of(TrophyEntity::new, BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    }
    public static TrophyKind headKind(ItemStack stack) {
        if (stack.is(Items.ZOMBIE_HEAD)) return TrophyKind.ZOMBIE;
        if (stack.is(Items.SKELETON_SKULL)) return TrophyKind.SKELETON;
        if (stack.is(Items.CREEPER_HEAD)) return TrophyKind.CREEPER;
        if (stack.is(Items.WITHER_SKELETON_SKULL)) return TrophyKind.WITHER_SKELETON;
        if (stack.is(STRAY_HEAD.get())) return TrophyKind.STRAY;
        if (stack.is(BOGGED_HEAD.get())) return TrophyKind.BOGGED;
        if (stack.is(Items.PIGLIN_HEAD)) return TrophyKind.PIGLIN;
        if (stack.is(Items.DRAGON_HEAD)) return TrophyKind.ENDER_DRAGON;
        if (stack.is(Items.PLAYER_HEAD)) return TrophyKind.PLAYER;
        return null;
    }
    public static Item headItem(TrophyKind kind) {
        return switch (kind) {
            case ZOMBIE -> Items.ZOMBIE_HEAD; case SKELETON -> Items.SKELETON_SKULL; case CREEPER -> Items.CREEPER_HEAD;
            case WITHER_SKELETON -> Items.WITHER_SKELETON_SKULL; case STRAY -> STRAY_HEAD.get(); case BOGGED -> BOGGED_HEAD.get();
            case PIGLIN -> Items.PIGLIN_HEAD; case ENDER_DRAGON -> Items.DRAGON_HEAD; case PLAYER -> Items.PLAYER_HEAD;
        };
    }
    public static void init() {}
    private Trophies() {}
}
