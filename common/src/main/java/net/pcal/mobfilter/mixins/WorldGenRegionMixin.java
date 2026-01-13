package net.pcal.mobfilter.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.pcal.mobfilter.MixinService;

@SuppressWarnings("ALL")
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "addFreshEntity", remap = false)
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        MixinService.get().WorldGenRegion_addFreshEntity((WorldGenRegion)(Object)this, entity, cir);
    }
}
