package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.pcal.mobfilter.MFService;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {

    @Inject(method = "isSpawnPositionOk", at = @At("RETURN"), cancellable = true)
    private static void mf_isSpawnPositionOk(EntityType<?> type, LevelReader levelReader, BlockPos pos, CallbackInfoReturnable<Boolean> ret) {
        if (ret.getReturnValue() == false) return;  // don't bother if minecraft already said no
        if (levelReader instanceof ServerLevel sl) {
            if (!MFService.getInstance().isSpawnAllowed(sl,  type, pos)) ret.setReturnValue(false);
        } else if (levelReader instanceof ServerLevelAccessor sla) {
            // This is typically (exclusively?) true during world generation - sla will be an instance of WorldGenRegion
            if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), type, pos)) ret.setReturnValue(false);
        } else {
            LogManager.getLogger(MFService.class).debug("Unexpected LevelReader: "+levelReader.getClass());
            return;
        }

    }
}
