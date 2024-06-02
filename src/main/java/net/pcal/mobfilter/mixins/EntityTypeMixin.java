package net.pcal.mobfilter.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.pcal.mobfilter.MFService;
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

    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;")
    private void mf_spawn(net.minecraft.server.level.ServerLevel serverLevel,
                          net.minecraft.core.BlockPos blockPos,
                          net.minecraft.world.entity.MobSpawnType mobSpawnType,
                          CallbackInfoReturnable<net.minecraft.world.entity.Entity> cir) {
        if (!MFService.getInstance().isSpawnAllowed2(serverLevel, (EntityType<? extends Mob>) (Object) this, blockPos, mobSpawnType)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;")
    private void mf_spawn(net.minecraft.server.level.ServerLevel serverLevel,
                          java.util.function.Consumer<?> ignored0,
                          net.minecraft.core.BlockPos blockPos,
                          net.minecraft.world.entity.MobSpawnType mobSpawnType,
                          boolean ignored1,
                          boolean ignored2,
                          CallbackInfoReturnable<net.minecraft.world.entity.Entity> cir) {
        if (!MFService.getInstance().isSpawnAllowed3(serverLevel, (EntityType<? extends Mob>) (Object) this, blockPos, mobSpawnType)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}

