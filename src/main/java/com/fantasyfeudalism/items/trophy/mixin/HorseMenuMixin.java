package com.fantasyfeudalism.items.trophy.mixin;
import com.fantasyfeudalism.items.mount.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractHorse.class)
abstract class HorseMenuMixin {
    @Inject(method="openCustomInventoryScreen",at=@At("HEAD"),cancellable=true)
    private void feudalItems$equipment(Player player,CallbackInfo ci){
        var horse=(AbstractHorse)(Object)this;
        if(MountEquipment.supported(horse)&&horse.isTamed()){
            if(player instanceof net.minecraft.server.level.ServerPlayer server)server.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,p)->new MountMenu(id,inv,MountEquipment.inventory(horse)),net.minecraft.network.chat.Component.literal("Mount Equipment")),buffer->buffer.writeVarInt(horse.getId()));
            ci.cancel();
        }
    }
}
