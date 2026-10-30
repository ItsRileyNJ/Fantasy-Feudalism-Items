package com.fantasyfeudalism.items.content;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;

public final class FishingTrapEntity extends BlockEntity {
    private final List<ItemStack> catches = new ArrayList<>();
    private int elapsed;
    public FishingTrapEntity(BlockPos pos, BlockState state) { super(Content.FISHING_ENTITY.get(), pos, state); }
    public List<ItemStack> catches() { return catches.stream().map(ItemStack::copy).toList(); }
    public int elapsed() { return elapsed; }
    public static void tick(Level world, BlockPos pos, BlockState state, FishingTrapEntity trap) {
        if (!(world instanceof ServerLevel level) || !state.getValue(BlockStateProperties.WATERLOGGED) || !level.getBiome(pos).is(BiomeTags.IS_RIVER)) return;
        trap.elapsed++;
        if (trap.elapsed < 3600) { if (trap.elapsed % 20 == 0) trap.setChanged(); return; }
        trap.elapsed = 0;
        if (trap.catches.size() < 8) trap.attempt(level);
        trap.setChanged();
    }
    public enum CatchType { FISH, JUNK, TREASURE, NOTHING }
    public static CatchType roll(java.util.function.DoubleSupplier random) {
        if(random.getAsDouble()<0.25)return CatchType.FISH;
        if(random.getAsDouble()>=0.25)return CatchType.NOTHING;
        return random.getAsDouble()<0.8?CatchType.JUNK:CatchType.TREASURE;
    }
    public void attempt(ServerLevel level) {
        if (catches.size() >= 8) return;
        var random = level.random;
        var outcome=roll(random::nextDouble);
        if (outcome==CatchType.FISH) {
            var fish = level.getBiome(worldPosition).value().getMobSettings().getMobs(MobCategory.WATER_AMBIENT).unwrap().stream()
                .map(spawn -> spawn.type).filter(type -> type == EntityType.SALMON || type == EntityType.COD || type == EntityType.TROPICAL_FISH || type == EntityType.PUFFERFISH).distinct().toList();
            if (!fish.isEmpty()) {
                var type = fish.get(random.nextInt(fish.size()));
                catches.add(new ItemStack(type == EntityType.SALMON ? Items.SALMON : type == EntityType.COD ? Items.COD : type == EntityType.PUFFERFISH ? Items.PUFFERFISH : Items.TROPICAL_FISH));
            }
        } else if (outcome==CatchType.JUNK||outcome==CatchType.TREASURE) {
            var table = level.getServer().reloadableRegistries().getLootTable(outcome==CatchType.JUNK ? BuiltInLootTables.FISHING_JUNK : BuiltInLootTables.FISHING_TREASURE);
            var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD)).create(LootContextParamSets.FISHING);
            var loot = table.getRandomItems(params);
            if (!loot.isEmpty()) { var item = loot.getFirst().copy(); item.setCount(1); catches.add(item); }
        }
        setChanged();
    }
    public void collect(Player player) {
        for (var item : catches) { var remaining = item.copy(); if (!player.getInventory().add(remaining)) player.drop(remaining, false); }
        catches.clear(); setChanged();
    }
    public void dropContents() { if (level != null) for (var item : catches) Block.popResource(level, worldPosition, item); catches.clear(); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); tag.putInt("Elapsed", elapsed); var list = new ListTag(); for (var stack : catches) list.add(stack.save(registries)); tag.put("Catches", list);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); elapsed = Math.max(0, Math.min(3599, tag.getInt("Elapsed"))); catches.clear();
        for (var entry : tag.getList("Catches", Tag.TAG_COMPOUND)) if (catches.size() < 8) ItemStack.parse(registries, entry).ifPresent(catches::add);
    }
}
