package com.fantasyfeudalism.items.trophy.mixin;
import com.fantasyfeudalism.items.trophy.Trophies;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(EnchantmentMenu.class)
abstract class EnchantTableMixin {
    @Shadow @Final private Container enchantSlots;
    @Shadow @Final public int[] costs;
    @Inject(method="getEnchantmentList", at=@At("HEAD"), cancellable=true)
    private void feudalItems$sigil(RegistryAccess registry, ItemStack stack, int option, int cost, CallbackInfoReturnable<List<EnchantmentInstance>> result) {
        if (stack.is(Trophies.HEADS)) result.setReturnValue(option == 2 && cost >= 30
            ? List.of(new EnchantmentInstance(registry.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Trophies.SIGIL), 1)) : List.of());
    }
    @Inject(method="slotsChanged", at=@At("TAIL"))
    private void feudalItems$onlyLevel30(Container container, CallbackInfo ci) {
        if (enchantSlots.getItem(0).is(Trophies.HEADS)) { costs[0] = 0; costs[1] = 0; if (costs[2] < 30) costs[2] = 0; }
    }
    @Inject(method="clickMenuButton", at=@At("HEAD"), cancellable=true)
    private void feudalItems$rejectOtherOptions(Player player, int option, CallbackInfoReturnable<Boolean> result) {
        if (enchantSlots.getItem(0).is(Trophies.HEADS) && (option != 2 || costs[2] < 30)) result.setReturnValue(false);
    }
}
