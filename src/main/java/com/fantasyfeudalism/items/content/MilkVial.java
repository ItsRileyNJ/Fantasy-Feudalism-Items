package com.fantasyfeudalism.items.content;

import java.util.ArrayList;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class MilkVial extends Item {
    public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){
        var player=event.getEntity();if(!player.level().isClientSide&&player.getFoodData().getFoodLevel()>=20)player.getPersistentData().remove("ff_milk_food_fraction");
    }
    public MilkVial(Properties properties) { super(properties); }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            for (var old : new ArrayList<>(entity.getActiveEffects())) {
                entity.removeEffect(old.getEffect());
                int newLevel = (old.getAmplifier() + 1) / 2;
                int duration = old.isInfiniteDuration() ? -1 : old.getDuration() / 2;
                if (newLevel > 0 && (duration == -1 || duration > 0))
                    entity.addEffect(new MobEffectInstance(old.getEffect(), duration, newLevel - 1, old.isAmbient(), old.isVisible(), old.showIcon()));
            }
            if (entity instanceof Player player) {
                var food = player.getFoodData();
                double credit = food.getFoodLevel() >= 20 ? 0 : player.getPersistentData().getDouble("ff_milk_food_fraction");
                double restored = 2.5 + credit;
                food.eat((int)Math.floor(restored), 0);
                player.getPersistentData().putDouble("ff_milk_food_fraction", food.getFoodLevel() >= 20 ? 0 : restored - Math.floor(restored));
            }
        }
        if (entity instanceof Player player && player.getAbilities().instabuild) return stack;
        stack.shrink(1);
        if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
        if (entity instanceof Player player && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
        return stack;
    }
}
