package com.fantasyfeudalism.items.trophy;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class HeadDrops {
    public static final ModConfigSpec SPEC;
    public static final java.util.List<ModConfigSpec.DoubleValue> CHANCES;
    static {
        var b = new ModConfigSpec.Builder(); var values = new java.util.ArrayList<ModConfigSpec.DoubleValue>();
        double[] defaults = {0.1, 0.4, 0.9, 1.1};
        for (int i = 0; i < 4; i++) values.add(b.comment("Percent probability per kill at Looting level " + i).defineInRange("looting" + i + "Percent", defaults[i], 0.0, 100.0));
        CHANCES = java.util.List.copyOf(values); SPEC = b.build();
    }
    public static void drops(LivingDropsEvent event) {
        var type = event.getEntity().getType(); TrophyKind kind;
        if (type == EntityType.ZOMBIE) kind = TrophyKind.ZOMBIE; else if (type == EntityType.SKELETON) kind = TrophyKind.SKELETON;
        else if (type == EntityType.CREEPER) kind = TrophyKind.CREEPER; else if (type == EntityType.WITHER_SKELETON) kind = TrophyKind.WITHER_SKELETON;
        else if (type == EntityType.STRAY) kind = TrophyKind.STRAY; else if (type == EntityType.BOGGED) kind = TrophyKind.BOGGED;
        else if (type == EntityType.PIGLIN) kind = TrophyKind.PIGLIN; else return;
        var entity = event.getEntity(); var item = Trophies.headItem(kind);
        boolean charged = event.getSource().getEntity() instanceof net.minecraft.world.entity.monster.Creeper creeper && creeper.isPowered();
        if (charged && event.getDrops().stream().anyMatch(drop -> drop.getItem().is(item))) return;
        event.getDrops().removeIf(drop -> drop.getItem().is(item));
        int looting = 0;
        if (event.getSource().getEntity() instanceof LivingEntity killer) looting = EnchantmentHelper.getItemEnchantmentLevel(entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer.getMainHandItem());
        double chance = CHANCES.get(Math.min(3, Math.max(0, looting))).get() / 100;
        if (entity.getRandom().nextDouble() < chance) event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(item)));
    }
}
