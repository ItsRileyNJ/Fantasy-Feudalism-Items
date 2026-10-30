package com.fantasyfeudalism.items.spawner;

import com.fantasyfeudalism.items.content.Content;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.network.chat.Component;

public final class SpawnerEyes {
    public static final Map<String,java.util.function.Supplier<Eye>> ITEMS=new LinkedHashMap<>();
    static {
        for(String mob:List.of("zombie","skeleton","spider","cave_spider","blaze","magma_cube"))ITEMS.put(mob,Content.ITEMS.register(mob+"_spawner_eye",()->new Eye(mob,new Item.Properties())));
    }
    public static final int RANGE=128;
    public static Optional<BlockPos> find(ServerLevel level,BlockPos origin,String mob){
        BlockPos best=null;double nearest=RANGE*RANGE;
        for(int x=(origin.getX()-RANGE)>>4;x<=(origin.getX()+RANGE)>>4;x++)for(int z=(origin.getZ()-RANGE)>>4;z<=(origin.getZ()+RANGE)>>4;z++){
            var chunk=level.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;
            for(var entity:chunk.getBlockEntities().values())if(entity instanceof SpawnerBlockEntity spawner){
                var data=spawner.getSpawner().save(new net.minecraft.nbt.CompoundTag());
                String id=data.getCompound("SpawnData").getCompound("entity").getString("id");
                if(!id.equals("minecraft:"+mob))continue;
                double distance=spawner.getBlockPos().distSqr(origin);
                if(distance<=nearest){nearest=distance;best=spawner.getBlockPos();}
            }
        }
        return Optional.ofNullable(best);
    }
    public static final class Eye extends Item {
        public final String mob;
        public Eye(String mob,Properties properties){super(properties);this.mob=mob;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            var stack=player.getItemInHand(hand);
            if(level instanceof ServerLevel server){
                var target=find(server,player.blockPosition(),mob);
                if(target.isEmpty()){player.displayClientMessage(Component.literal("No matching spawner within 128 blocks in loaded chunks."),true);return InteractionResultHolder.fail(stack);}
                var eye=new EyeOfEnder(level,player.getX(),player.getY(.5),player.getZ());eye.setItem(stack.copyWithCount(1));eye.signalTo(target.get());level.addFreshEntity(eye);
                level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.ENDER_EYE_LAUNCH,net.minecraft.sounds.SoundSource.PLAYERS,1,1);
                if(!player.getAbilities().instabuild)stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
    }
    public static void init(){}
}
