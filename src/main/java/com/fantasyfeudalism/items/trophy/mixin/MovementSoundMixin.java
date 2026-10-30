package com.fantasyfeudalism.items.trophy.mixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Entity.class)
abstract class MovementSoundMixin {
    @ModifyVariable(method="playSound(Lnet/minecraft/sounds/SoundEvent;FF)V",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private float feudalItems$quietMovement(float volume,SoundEvent sound,float original,float pitch){
        if((Object)this instanceof net.minecraft.world.entity.player.Player player&&player.getData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY)==com.fantasyfeudalism.items.trophy.TrophyKind.CREEPER.ordinal()){
            String path=sound.getLocation().getPath();if(path.endsWith(".step")||path.contains(".swim")||path.contains(".splash")||path.endsWith(".fall")||path.endsWith("_fall")||path.contains(".jump")||path.endsWith(".flap"))return volume*.5f;
        }
        return volume;
    }
}
