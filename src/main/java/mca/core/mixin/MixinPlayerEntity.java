package mca.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.core.minecraft.entity.village.VillageSpawnQueue;
import mca.items.BabyItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity extends LivingEntity {
    private MixinPlayerEntity() { super(null, null); }

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onOnDeath(DamageSource cause, CallbackInfo info) {
        VillageSpawnQueue.getInstance().onPlayerDeath(cause, (PlayerEntity)(Object)this);
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true)
    private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> info) {
        if (stack.getItem() instanceof BabyItem && !((BabyItem)stack.getItem()).onDropped(stack, (PlayerEntity)(Object)this)) {
            info.setReturnValue(null);
        }
    }
}