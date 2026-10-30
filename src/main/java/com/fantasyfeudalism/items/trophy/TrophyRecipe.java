package com.fantasyfeudalism.items.trophy;

import com.fantasyfeudalism.items.content.Content;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class TrophyRecipe extends CustomRecipe {
    public TrophyRecipe(CraftingBookCategory category) { super(category); }
    private TrophyKind result(CraftingInput input) {
        TrophyKind found = null; boolean mount = false;
        for (var stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (stack.is(Content.TROPHY_MOUNT.get()) && !mount) mount = true;
            else {
                var kind = Trophies.headKind(stack);
                if (found != null || kind == null || stack.getOrDefault(DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).entrySet().stream().noneMatch(e -> e.getKey().is(Trophies.SIGIL) && e.getIntValue() > 0)) return null;
                found = kind;
            }
        }
        return mount ? found : null;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return result(input) != null; }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) { var kind = result(input); return kind == null ? ItemStack.EMPTY : new ItemStack(Trophies.BLOCKS.get(kind).get()); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public RecipeSerializer<?> getSerializer() { return TrophyRecipes.SERIALIZER.get(); }
}
