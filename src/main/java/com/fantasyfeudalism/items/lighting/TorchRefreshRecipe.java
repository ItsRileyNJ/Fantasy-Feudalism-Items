package com.fantasyfeudalism.items.lighting;

import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Each occupied torch slot consumes one torch and one use of the returned tool. */
public final class TorchRefreshRecipe extends CustomRecipe {
    public static final java.util.function.Supplier<RecipeSerializer<TorchRefreshRecipe>> SERIALIZER=com.fantasyfeudalism.items.trophy.TrophyRecipes.REGISTRY.register("torch_refresh",()->new SimpleCraftingRecipeSerializer<>(TorchRefreshRecipe::new));
    public TorchRefreshRecipe(CraftingBookCategory category){super(category);}
    public static void init(){}
    private ItemStack output(CraftingInput input){
        ItemStack result=ItemStack.EMPTY,tool=ItemStack.EMPTY;int count=0;
        for(var stack:input.items()){
            if(stack.isEmpty())continue;
            if(stack.is(Items.FLINT_AND_STEEL)){if(!tool.isEmpty()||stack.getCount()!=1)return ItemStack.EMPTY;tool=stack;}
            else if(stack.is(Items.TORCH)||stack.is(Items.SOUL_TORCH)){
                var full=stack.copyWithCount(1);LightItemFuel.setStage(full,3,true);
                if(result.isEmpty())result=full;else if(!ItemStack.isSameItemSameComponents(result,full))return ItemStack.EMPTY;
                count++;
            }else return ItemStack.EMPTY;
        }
        if(tool.isEmpty()||count==0||tool.isDamageableItem()&&tool.getMaxDamage()-tool.getDamageValue()<count)return ItemStack.EMPTY;
        result.setCount(count);return result;
    }
    @Override public boolean matches(CraftingInput input,Level level){return !output(input).isEmpty();}
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries){return output(input);}
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput input){
        var result=NonNullList.withSize(input.size(),ItemStack.EMPTY);int count=output(input).getCount();if(count==0)return result;
        for(int i=0;i<input.size();i++)if(input.getItem(i).is(Items.FLINT_AND_STEEL)){
            var tool=input.getItem(i).copyWithCount(1);var player=net.neoforged.neoforge.common.CommonHooks.getCraftingPlayer();
            if(player!=null&&player.level() instanceof net.minecraft.server.level.ServerLevel level){
                var breakingStack=tool.copy();
                tool.hurtAndBreak(count,level,player,item->net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player,breakingStack,null));
                LightItemFuel.resetProgressForItem(player,output(input).getItem());
            }else if(tool.isDamageableItem()){
                tool.setDamageValue(tool.getDamageValue()+count);if(tool.getDamageValue()>=tool.getMaxDamage())tool=ItemStack.EMPTY;
            }
            result.set(i,tool);break;
        }
        return result;
    }
    @Override public boolean canCraftInDimensions(int width,int height){return width*height>=2;}
    @Override public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}
}
