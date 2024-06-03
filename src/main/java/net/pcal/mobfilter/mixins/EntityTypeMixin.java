package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.pcal.mobfilter.MFMixinBodies;
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

    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;")
    private void mf_spawn(ServerLevel serverLevel,
                          BlockPos blockPos,
                          MobSpawnType mobSpawnType,
                          CallbackInfoReturnable<Entity> cir) {
        MFMixinBodies.EntityTypeMixin_spawn((EntityType<? extends Mob>) (Object) this, serverLevel, blockPos, mobSpawnType, cir);
    }

    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;")
    private void mf_spawn(ServerLevel serverLevel,
                          Consumer<?> ignored0,
                          BlockPos blockPos,
                          MobSpawnType mobSpawnType,
                          boolean ignored1,
                          boolean ignored2,
                          CallbackInfoReturnable<Entity> cir) {
        MFMixinBodies.EntityTypeMixin_spawn((EntityType<? extends Mob>) (Object) this, serverLevel, ignored0, blockPos, mobSpawnType, ignored1, ignored2, cir);
    }
}

