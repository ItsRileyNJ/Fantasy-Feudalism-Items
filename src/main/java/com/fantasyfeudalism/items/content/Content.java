package com.fantasyfeudalism.items.content;

import com.fantasyfeudalism.items.FeudalItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class Content {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FeudalItems.ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FeudalItems.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FeudalItems.ID);
    public static final DeferredItem<Item> KELP_OIL = ITEMS.registerSimpleItem("kelp_oil", new Item.Properties().stacksTo(16));
    public static final DeferredItem<MilkVial> MILK = ITEMS.register("vial_of_milk", () -> new MilkVial(new Item.Properties().stacksTo(16)));
    public static final DeferredBlock<MilkCauldron> MILK_CAULDRON = BLOCKS.register("milk_cauldron", () -> new MilkCauldron(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)));
    public static final DeferredBlock<FishingTrap> FISHING_TRAP = BLOCKS.register("fishing_trap", () -> new FishingTrap(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).noOcclusion()));
    public static final DeferredItem<BlockItem> FISHING_TRAP_ITEM = ITEMS.register("fishing_trap", () -> new FishingTrapItem(FISHING_TRAP.get(),new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishingTrapEntity>> FISHING_ENTITY = BLOCK_ENTITIES.register("fishing_trap", () -> BlockEntityType.Builder.of(FishingTrapEntity::new, FISHING_TRAP.get()).build(null));
    public static final DeferredItem<BoneDagger> BONE_DAGGER = ITEMS.register("bone_dagger", () -> new BoneDagger(new Item.Properties()));
    public static final DeferredItem<Item> TROPHY_MOUNT = ITEMS.registerSimpleItem("trophy_mount");
    public static void register(IEventBus bus) { ITEMS.register(bus); BLOCKS.register(bus); BLOCK_ENTITIES.register(bus); }
    public static void creative(java.util.function.Consumer<ItemStack> output) {
        output.accept(new ItemStack(com.fantasyfeudalism.items.mount.MountContent.SADDLEBAG.get()));
        output.accept(new ItemStack(com.fantasyfeudalism.items.mount.MountContent.HALF.get()));
        for(var shoe:java.util.List.of(com.fantasyfeudalism.items.mount.MountContent.COPPER,com.fantasyfeudalism.items.mount.MountContent.IRON,com.fantasyfeudalism.items.mount.MountContent.GOLD,com.fantasyfeudalism.items.mount.MountContent.DIAMOND,com.fantasyfeudalism.items.mount.MountContent.NETHERITE))output.accept(new ItemStack(shoe.get()));
        for(var eye:com.fantasyfeudalism.items.spawner.SpawnerEyes.ITEMS.values())output.accept(new ItemStack(eye.get()));
        output.accept(new ItemStack(BONE_DAGGER.get()));
        for (var block : com.fantasyfeudalism.items.trophy.Trophies.BLOCKS.values()) output.accept(new ItemStack(block.get()));
        output.accept(new ItemStack(com.fantasyfeudalism.items.trophy.Trophies.STRAY_HEAD.get()));
        output.accept(new ItemStack(com.fantasyfeudalism.items.trophy.Trophies.BOGGED_HEAD.get()));
        output.accept(new ItemStack(KELP_OIL.get())); output.accept(new ItemStack(MILK.get()));
        output.accept(new ItemStack(FISHING_TRAP.get())); output.accept(new ItemStack(TROPHY_MOUNT.get()));
    }
    private Content() {}
}
