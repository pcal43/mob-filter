package net.pcal.mobfilter.forge.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.pcal.mobfilter.forge.ForgeMixinHandlers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@SuppressWarnings("ALL")
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "addFreshEntity")
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ForgeMixinHandlers.get().ServerLevel_addFreshEntity((ServerLevel)(Object)this, entity, cir);
    }
}
