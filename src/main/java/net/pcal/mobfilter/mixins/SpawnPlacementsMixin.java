package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void mf_checkSpawnRules(EntityType<?> entityType,
                                        ServerLevelAccessor sla,
                                        MobSpawnType mobSpawnType,
                                        BlockPos blockPos,
                                        RandomSource ignored,
                                        CallbackInfoReturnable cir) {
        if (!MFService.getInstance().isSpawnAllowed4(sla.getLevel(), (EntityType<? extends Mob>) entityType, blockPos, mobSpawnType)) {
            cir.setReturnValue(false);
        }
    }
}
