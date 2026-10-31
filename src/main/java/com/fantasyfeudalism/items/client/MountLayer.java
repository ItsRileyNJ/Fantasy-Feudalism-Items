package com.fantasyfeudalism.items.client;
import com.fantasyfeudalism.items.content.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.*;
public final class MountLayer<T extends AbstractHorse,M extends HorseModel<T>> extends RenderLayer<T,M> {
    public MountLayer(RenderLayerParent<T,M> parent){super(parent);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,T horse,float swing,float amplitude,float partial,float age,float yaw,float pitch){
        var bag=com.fantasyfeudalism.items.mount.MountVisuals.bag(horse);
        var lantern=com.fantasyfeudalism.items.mount.MountVisuals.lantern(horse);
        if(bag.isEmpty()&&lantern.isEmpty())return;
        pose.pushPose();((com.fantasyfeudalism.items.client.mixin.HorseBodyAccessor)getParentModel()).feudalItems$body().translateAndRotate(pose);
        if(horse.isBaby())pose.scale(.5f,.5f,.5f);
        if(!bag.isEmpty()){renderItem(bag,pose,buffers,light,horse,-.48,0,-.12,.6f);renderItem(bag,pose,buffers,light,horse,.48,0,-.12,.6f);}
        if(!lantern.isEmpty())renderItem(lantern,pose,buffers,light,horse,.47,-.05,-.65,.45f);
        pose.popPose();
    }
    private void renderItem(ItemStack item,PoseStack pose,MultiBufferSource buffers,int light,T horse,double x,double y,double z,float scale){
        pose.pushPose();pose.translate(x,y,z);pose.scale(scale,-scale,-scale);Minecraft.getInstance().getItemRenderer().renderStatic(item,ItemDisplayContext.FIXED,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffers,horse.level(),horse.getId());pose.popPose();
    }
}
