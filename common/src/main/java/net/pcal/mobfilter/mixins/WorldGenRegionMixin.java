package net.pcal.mobfilter.mixins;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.pcal.mobfilter.fabric.FabricMixinHandlers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "addFreshEntity")
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        FabricMixinHandlers.get().WorldGenRegion_addFreshEntity((WorldGenRegion)(Object)this, entity, cir);
    }
}
