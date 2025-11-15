package net.pcal.mobfilter.forge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.pcal.mobfilter.forge.ForgeMixinHandlers;

@SuppressWarnings("ALL")
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {

    @Inject(method = "addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ForgeMixinHandlers.get().WorldGenRegion_addFreshEntity((WorldGenRegion)(Object)this, entity, cir);
    }
}
