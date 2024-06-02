package net.pcal.mobfilter.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.pcal.mobfilter.MFService;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This catches some oddball cases like BUCKET, DISPENSER and SPAWN_EGG spawns.
 */
@SuppressWarnings("ALL")
@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType);Lnet.minecraft.world.Entity")
    public void mf_spawn(net.minecraft.server.level.ServerLevel serverLevel,
                         net.minecraft.core.BlockPos blockPos,
                         net.minecraft.world.entity.MobSpawnType mobSpawnType,
                         CallbackInfoReturnable cir) {
        if ((Object)this instanceof Mob mob) {
            if (!MFService.getInstance().isSpawnAllowed(serverLevel, mob.getType(), blockPos, mobSpawnType)) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }

    @Nullable
    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;Z;Z);Lnet.minecraft.world.Entity")
    public void mf_spawn(net.minecraft.server.level.ServerLevel serverLevel,
                         java.util.function.Consumer<?> consumer,
                         net.minecraft.core.BlockPos blockPos,
                         net.minecraft.world.entity.MobSpawnType mobSpawnType,
                         boolean bl,
                         boolean bl2,
                         CallbackInfoReturnable cir) {
        if ((Object)this instanceof Mob mob) {
            if (!MFService.getInstance().isSpawnAllowed(serverLevel, mob.getType(), mob.blockPosition(), mobSpawnType)) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }
}
