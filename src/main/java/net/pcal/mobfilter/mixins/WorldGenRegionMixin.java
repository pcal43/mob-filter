package net.pcal.mobfilter.mixins;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "addFreshEntity")
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!MFService.getInstance().isSpawnAllowed(((WorldGenRegion)(Object)this).getLevel(), entity)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            cir.setReturnValue(false);
        }
    }
}
