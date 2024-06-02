package net.pcal.mobfilter.mixins.disabled;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.pcal.mobfilter.MFService;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * DISABLED.  This is hopefully redundant (we try to catch spawn events earlier in the other mx with earlier checks)
 * and may have unanticipated side effects, so disabled in mixins.json.  Leaving it here as a note.
 */
@SuppressWarnings("ALL")
@Mixin(PathfinderMob.class)
public abstract class PathfinderMobMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private void mf_checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType mobSpawnType, CallbackInfoReturnable ci) {
        final Mob mob = (Mob) (Object) this;
        if (levelAccessor instanceof ServerLevelAccessor sla) {
            if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), (EntityType<? extends Mob>)mob.getType(), mob.blockPosition(), mobSpawnType)) ci.cancel();
        } else {
            LogManager.getLogger(MFService.class).debug("Unexpected LevelAccessor: " + levelAccessor.getClass());
        }
    }
}
