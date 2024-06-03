package net.pcal.mobfilter.mixins.disabled;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * DISABLED.  This is a very low-level, last-minute intercept all entity additions.  This is hopefully redundant
 * (we try to catch spawn events earlier in the other mx with earlier checks) and may have unanticipated side effects,
 * so disabled in mixins.json.  Leaving it here as a note.
 */
@SuppressWarnings("ALL")
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable ci) {
    }
}
