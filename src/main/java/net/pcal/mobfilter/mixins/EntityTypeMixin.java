package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * Intercept calls to EntityType.create so we can try to track MobSpawnType.
 */
@SuppressWarnings("ALL")
@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(at = @At("RETURN"), method = "create(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;", cancellable = true)
    private void mf_create(ServerLevel serverLevel, Consumer ignored, BlockPos blockPos, MobSpawnType spawnType, boolean bl, boolean bl2, CallbackInfoReturnable<Entity> cir) {
        I THINK YOU NEED TO GET IT FROM FINALIZESPAWN INSTEAD
        MFService.getInstance().notifyEntityCreate(serverLevel, spawnType, cir.getReturnValue());
    }
}

