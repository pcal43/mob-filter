package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.pcal.mobfilter.MFService;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * This catches some oddball cases like BUCKET, DISPENSER and SPAWN_EGG spawns.
 */
@SuppressWarnings("ALL")
@Mixin(EntityType.class)
public abstract class EntityTypeMixin {


    @Nullable
    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    public void mf_spawn(ServerLevel serverLevel, BlockPos blockPos, MobSpawnType mobSpawnType) {
    }

    @Nullable
    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    public void mf_spawn(ServerLevel serverLevel,
                         @Nullable Consumer<?> consumer,
                         BlockPos blockPos,
                         MobSpawnType mobSpawnType,
                         boolean bl,
                         boolean bl2,
                         CallbackInfoReturnable ci) {
        if ((Object)this instanceof Mob mob) {
            if (!MFService.getInstance().isSpawnAllowed(serverLevel, mob.getType(), mob.blockPosition(), mobSpawnType)) ci.cancel();
        }
    }
}
