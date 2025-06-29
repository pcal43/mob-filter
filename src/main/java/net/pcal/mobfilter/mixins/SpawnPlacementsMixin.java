package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.pcal.mobfilter.MFMixinBodies;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This intercepts most common spawns.
 */
@SuppressWarnings("ALL")
@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void mf_checkSpawnRules(EntityType<?> entityType,
                                           ServerLevelAccessor sla,
                                           EntitySpawnReason spawnReason,
                                           BlockPos blockPos,
                                           RandomSource ignored,
                                           CallbackInfoReturnable cir) {
        //MFMixinBodies.SpawnPlacementsMixin_checkSpawnRules(entityType, sla, spawnReason, blockPos, ignored, cir);
    }
}
