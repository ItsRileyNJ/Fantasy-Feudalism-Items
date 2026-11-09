package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.lighting.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class LightFuelStageTests {
    private static TorchRefreshRecipe recipe(){return new TorchRefreshRecipe(CraftingBookCategory.MISC);}
    private static ItemStack torch(int stage,int count){var stack=new ItemStack(Items.TORCH,count);LightItemFuel.setStage(stack,stage,stage>0);return stack;}
    @GameTest(template="empty")
    public static void used_lights_stack_by_stage_and_legacy_countdowns_are_removed(GameTestHelper h){
        for(var item:List.of(Items.TORCH,Items.LANTERN)){
            long cap=LightFuel.capacity(LightItemFuel.state(new ItemStack(item)));
            var old=new ItemStack(item);CustomData.update(DataComponents.CUSTOM_DATA,old,tag->{tag.putLong("ff_fuel",cap-20);tag.putBoolean("ff_off",false);tag.putLong("ff_last_burn_tick",1234);tag.putString("ff_last_burn_dimension","old");});
            LightItemFuel.normalize(old);
            h.assertTrue(ItemStack.isSameItemSameComponents(old,new ItemStack(item)),"Used full-stage lights merge with fresh lights without an empty custom-data component");
            for(int stage=1;stage<=3;stage++){
                var a=new ItemStack(item);var b=new ItemStack(item);
                LightItemFuel.set(a,LightItemFuel.boundary(cap,stage),true);
                LightItemFuel.set(b,LightItemFuel.boundary(cap,stage)-20,true);
                h.assertTrue(ItemStack.isSameItemSameComponents(a,b),"Different countdowns within a stage have identical stack components");
            }
        }
        var named=torch(1,1);named.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My torch"));
        LightItemFuel.setStage(named,3,true);h.assertTrue(named.has(DataComponents.CUSTOM_NAME),"Fuel normalization preserves unrelated item components");h.succeed();
    }
    @GameTest(template="empty")
    public static void three_fuel_budgets_sum_to_the_original_configured_lifetime(GameTestHelper h){
        for(var item:List.of(Items.TORCH,Items.LANTERN)){
            long cap=LightFuel.capacity(LightItemFuel.state(new ItemStack(item)));long sum=0,min=Long.MAX_VALUE,max=0;
            for(int stage=1;stage<=3;stage++){
                long budget=LightItemFuel.boundary(cap,stage)-LightItemFuel.boundary(cap,stage-1);sum+=budget;min=Math.min(min,budget);max=Math.max(max,budget);
                h.assertTrue(LightItemFuel.stageFor(LightItemFuel.boundary(cap,stage),cap)==stage,"Fuel-stage boundary maps back to its stage");
            }
            h.assertTrue(sum==cap&&max-min<=1,"The three stages are equal thirds, with no loss of configured total duration");
        }
        for(var item:List.of(Items.SOUL_TORCH,Items.SOUL_LANTERN))h.assertTrue(LightItemFuel.remaining(new ItemStack(item))==Long.MAX_VALUE,"Soul lights retain unlimited fuel");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void burning_crosses_stage_boundaries_without_putting_timestamps_on_items(GameTestHelper h){
        h.succeedWhen(()->{
            h.assertTrue(h.getLevel().getGameTime()%20==0,"Wait for the normal fuel clock");
            long cap=LightFuel.capacity(Blocks.TORCH.defaultBlockState());
            for(int stage=3;stage>0;stage--){
                var stack=torch(stage,8);var drop=new ItemEntity(h.getLevel(),0,200,0,stack);
                var entry=new CompoundTag();entry.putString("Item","minecraft:torch");entry.putInt("Stage",stage);
                entry.putLong("Elapsed",LightItemFuel.boundary(cap,stage)-LightItemFuel.boundary(cap,stage-1)-20);
                var root=new CompoundTag();root.put("dropped",entry);drop.getPersistentData().put("ff_light_progress",root);
                LightItemFuel.advance(stack,drop);h.assertTrue(LightItemFuel.stage(stack)==stage-1,"Completing a third advances to the next shared stage");
                LightItemFuel.advance(stack,drop);h.assertTrue(LightItemFuel.stage(stack)==stage-1,"Duplicate tick hooks cannot burn a stage twice");
                var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe();
                h.assertFalse(tag.contains("ff_fuel")||tag.contains("ff_last_burn_tick")||tag.contains("ff_last_burn_dimension"),"No unique countdown or timestamp is written to items");
                if(stage==1)h.assertFalse(LightItemFuel.lit(stack),"Last stage burns out instead of refilling");
            }
        });
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void holder_countdowns_survive_save_without_splitting_light_stacks(GameTestHelper h){
        var restored=new ItemEntity[1];var firstTick=new long[]{-1};
        h.succeedWhen(()->{
            h.assertTrue(h.getLevel().getGameTime()%20==0,"Wait for fuel clock");
            if(restored[0]==null){
                var original=new ItemEntity(h.getLevel(),0,200,0,new ItemStack(Items.TORCH));LightItemFuel.advance(original.getItem(),original);
                var saved=original.saveWithoutId(new CompoundTag());
                restored[0]=new ItemEntity(h.getLevel(),0,200,0,new ItemStack(Items.TORCH));restored[0].load(saved);
                LightItemFuel.advance(restored[0].getItem(),restored[0]);firstTick[0]=h.getLevel().getGameTime();
                h.assertTrue(restored[0].getPersistentData().getCompound("ff_light_progress").getCompound("dropped").getLong("Elapsed")==20,"Saving and restoring retains progress and the duplicate-tick guard");
            }
            h.assertTrue(h.getLevel().getGameTime()>firstTick[0],"Wait for the next loaded second");
            LightItemFuel.advance(restored[0].getItem(),restored[0]);
            h.assertTrue(restored[0].getPersistentData().getCompound("ff_light_progress").getCompound("dropped").getLong("Elapsed")==40,"A restored holder continues its previous countdown");
            h.assertTrue(ItemStack.isSameItemSameComponents(restored[0].getItem(),new ItemStack(Items.TORCH)),"Persisting the countdown does not add any unique item components");
        });
    }
    @GameTest(template="empty")
    public static void torch_refresh_recipe_returns_full_torches_and_charges_each_input_slot(GameTestHelper h){
        var tool=new ItemStack(Items.FLINT_AND_STEEL);var input=CraftingInput.of(3,1,List.of(torch(0,64),torch(1,32),tool));
        var recipe=recipe();h.assertTrue(recipe.matches(input,h.getLevel()),"Mixed fuel stages of the same torch can be refreshed together");
        var result=recipe.assemble(input,h.getLevel().registryAccess());
        h.assertTrue(result.getCount()==2&&ItemStack.isSameItemSameComponents(result,new ItemStack(Items.TORCH)),"Each occupied slot contributes one full, stackable torch");
        var remainders=recipe.getRemainingItems(input);
        h.assertTrue(remainders.get(2).is(Items.FLINT_AND_STEEL)&&remainders.get(2).getDamageValue()==2,"Tool costs one durability per refreshed torch and is returned");
        h.assertTrue(tool.getDamageValue()==0&&input.getItem(0).getCount()==64,"Preview and remainders do not mutate crafting inputs");
        h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).isPresent(),"The refresh recipe is actually loaded by Minecraft");h.succeed();
    }
    @GameTest(template="empty")
    public static void torch_refresh_refuses_insufficient_durability_and_breaks_on_last_use(GameTestHelper h){
        var tool=new ItemStack(Items.FLINT_AND_STEEL);tool.setDamageValue(tool.getMaxDamage()-1);var recipe=recipe();
        h.assertFalse(recipe.matches(CraftingInput.of(3,1,List.of(torch(0,1),torch(1,1),tool)),h.getLevel()),"A batch cannot refresh more torches than its tool can service");
        var last=CraftingInput.of(2,1,List.of(torch(0,1),tool));
        h.assertTrue(recipe.matches(last,h.getLevel())&&recipe.getRemainingItems(last).get(1).isEmpty(),"Last durability refreshes one torch and consumes the broken tool");
        h.assertFalse(recipe.matches(CraftingInput.of(2,1,List.of(new ItemStack(Items.LANTERN),new ItemStack(Items.FLINT_AND_STEEL))),h.getLevel()),"Lanterns still need oil, not flint and steel");
        var named=torch(1,1);named.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Named"));
        h.assertFalse(recipe.matches(CraftingInput.of(3,1,List.of(named,torch(1,1),new ItemStack(Items.FLINT_AND_STEEL))),h.getLevel()),"A batch cannot erase different custom names");h.succeed();
    }
    @GameTest(template="empty")
    public static void manual_stack_refresh_and_crafting_have_equal_per_torch_wear(GameTestHelper h){
        var player=h.makeMockPlayer(GameType.SURVIVAL);var stack=torch(1,3);var container=new SimpleContainer(stack);var tool=new ItemStack(Items.FLINT_AND_STEEL);
        h.assertTrue(stack.overrideOtherStackedOnMe(tool,new Slot(container,0,0,0),ClickAction.SECONDARY,player,net.minecraft.world.entity.SlotAccess.NULL),"Manual flint-and-steel servicing handles a torch stack");
        h.assertTrue(tool.getDamageValue()==3&&LightItemFuel.stage(stack)==3,"Manually refreshing three torches costs three durability");
        var input=CraftingInput.of(2,2,List.of(torch(0,1),torch(1,1),torch(2,1),new ItemStack(Items.FLINT_AND_STEEL)));
        net.neoforged.neoforge.common.CommonHooks.setCraftingPlayer(player);
        try{h.assertTrue(recipe().getRemainingItems(input).get(3).getDamageValue()==tool.getDamageValue(),"Crafting uses the same player-aware damage path and per-torch cost");}
        finally{net.neoforged.neoforge.common.CommonHooks.setCraftingPlayer(null);}
        var pos=h.absolutePos(new BlockPos(3,2,3));h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.TORCH.defaultBlockState());
        LightFuel.setRemaining(h.getLevel(),pos,20);var handTool=new ItemStack(Items.FLINT_AND_STEEL);player.setItemInHand(InteractionHand.MAIN_HAND,handTool);
        var event=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));LightInteractions.interact(event);
        h.assertTrue(LightFuel.remaining(h.getLevel(),pos)==LightFuel.capacity(Blocks.TORCH.defaultBlockState())&&handTool.getDamageValue()==1,"A lit placed torch can have its timer reset for one durability");h.succeed();
    }
    @GameTest(template="empty")
    public static void repeated_stack_crafting_cannot_refresh_torches_for_free(GameTestHelper h){
        var tool=new ItemStack(Items.FLINT_AND_STEEL);int refreshed=0;
        while(!tool.isEmpty()){
            var input=CraftingInput.of(2,1,List.of(torch(0,64-refreshed),tool));
            h.assertTrue(recipe().matches(input,h.getLevel()),"Each remaining durability allows one more craft");
            refreshed+=recipe().assemble(input,h.getLevel().registryAccess()).getCount();tool=recipe().getRemainingItems(input).get(1);
        }
        h.assertTrue(refreshed==new ItemStack(Items.FLINT_AND_STEEL).getMaxDamage(),"Bulk crafting spends exactly one tool's durability for the corresponding torch count");h.succeed();
    }
}
