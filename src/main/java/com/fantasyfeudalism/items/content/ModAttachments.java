package com.fantasyfeudalism.items.content;
import com.fantasyfeudalism.items.FeudalItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,FeudalItems.ID);
    public static final java.util.function.Supplier<AttachmentType<CompoundTag>> MOUNT=TYPES.register("mount_visuals",()->AttachmentType.builder(()->new CompoundTag()).serialize(CompoundTag.CODEC).sync(ByteBufCodecs.COMPOUND_TAG).build());
    public static final java.util.function.Supplier<AttachmentType<Integer>> TROPHY=TYPES.register("active_trophy",()->AttachmentType.builder(()->-1).serialize(com.mojang.serialization.Codec.INT).sync(ByteBufCodecs.VAR_INT).build());
}
