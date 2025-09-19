package net.pcal.mobfilter.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.pcal.mobfilter.MFService.MinecraftThreadType.SERVER;

@SuppressWarnings("ALL")
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    public abstract boolean addEntity(Entity entity);

    @Inject(at = @At("HEAD"), cancellable = true, method = "addFreshEntity")
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!MFService.get().isSpawnAllowed((ServerLevel) (Object) this, entity, SERVER)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            cir.setReturnValue(false);
        }
    }
}
