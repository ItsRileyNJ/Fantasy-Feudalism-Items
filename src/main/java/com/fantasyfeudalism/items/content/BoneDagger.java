package com.fantasyfeudalism.items.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

public final class BoneDagger extends SwordItem {
    private static final Tier TIER = new Tier() {
        public int getUses() { return 100; }
        public float getSpeed() { return Tiers.WOOD.getSpeed(); }
        public float getAttackDamageBonus() { return 0; }
        public TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() { return Tiers.WOOD.getIncorrectBlocksForDrops(); }
        public int getEnchantmentValue() { return Tiers.WOOD.getEnchantmentValue(); }
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.FLINT); }
    };
    public BoneDagger(Properties properties) {
        super(TIER, properties.durability(100).attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -0.8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("fantasy_feudalism_items", "dagger_reach"), -0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build()));
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) { tooltip.add(Component.literal("20% Armor Penetration")); }
}
