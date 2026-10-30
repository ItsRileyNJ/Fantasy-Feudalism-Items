package com.fantasyfeudalism.items.trophy;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Permanent placement history survives player death, trophy destruction and chunk unloading. */
public final class TrophyLedger extends SavedData {
    private final Map<UUID, EnumSet<TrophyKind>> placed = new HashMap<>();
    private final Map<UUID, TrophyKind> active = new HashMap<>();
    public static TrophyLedger get(ServerLevel level) { return level.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(TrophyLedger::new, TrophyLedger::load), "fantasy_feudalism_trophies"); }
    public boolean hasPlaced(UUID owner, TrophyKind kind) { return placed.getOrDefault(owner, EnumSet.noneOf(TrophyKind.class)).contains(kind); }
    public boolean recordPlacement(UUID owner, TrophyKind kind) {
        boolean added = placed.computeIfAbsent(owner, ignored -> EnumSet.noneOf(TrophyKind.class)).add(kind); if (added) setDirty(); return added;
    }
    public TrophyKind active(UUID owner) { return active.get(owner); }
    public void attune(UUID owner, TrophyKind kind) { active.put(owner, kind); setDirty(); }
    public void clear(UUID owner) { active.remove(owner); setDirty(); }
    public static TrophyLedger load(CompoundTag tag, HolderLookup.Provider registries) {
        var ledger = new TrophyLedger();
        for (var value : tag.getList("Players", Tag.TAG_COMPOUND)) {
            var player = (CompoundTag) value; if (!player.hasUUID("Owner")) continue;
            var owner = player.getUUID("Owner");
            for (var kind : TrophyKind.values()) if (player.getBoolean("Placed_" + kind.id())) ledger.recordPlacement(owner, kind);
            if (player.contains("Active")) try { ledger.active.put(owner, TrophyKind.valueOf(player.getString("Active"))); } catch (IllegalArgumentException ignored) {}
        }
        return ledger;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        placed.forEach((owner, kinds) -> { var player = new CompoundTag(); player.putUUID("Owner", owner);
            for (var kind : kinds) player.putBoolean("Placed_" + kind.id(), true);
            if (active.containsKey(owner)) player.putString("Active", active.get(owner).name()); list.add(player); });
        tag.put("Players", list); return tag;
    }
}
