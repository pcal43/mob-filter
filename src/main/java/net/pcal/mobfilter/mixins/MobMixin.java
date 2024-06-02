package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.pcal.mobfilter.MFService;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void mf_checkMobSpawnRules(EntityType<? extends Mob> entityType,
                                              LevelAccessor levelAccessor,
                                              MobSpawnType mobSpawnType,
                                              BlockPos blockPos,
                                              RandomSource ignored,
                                              CallbackInfoReturnable ci) {
        if (levelAccessor instanceof ServerLevelAccessor sla) {
            if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), entityType, blockPos, mobSpawnType)) ci.cancel();
        } else {
            LogManager.getLogger(MFService.class).debug("Unexpected LevelAccessor: " + levelAccessor.getClass());
        }
    }

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private void mf_checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType mobSpawnType, CallbackInfoReturnable ci) {
        final Mob mob = (Mob) (Object) this;
        if (levelAccessor instanceof ServerLevelAccessor sla) {
            if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), mob.getType(), mob.blockPosition(), mobSpawnType)) ci.cancel();
        } else {
            LogManager.getLogger(MFService.class).debug("Unexpected LevelAccessor: " + levelAccessor.getClass());
        }
    }
}
