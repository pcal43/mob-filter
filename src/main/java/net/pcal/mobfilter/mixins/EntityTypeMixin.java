package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.pcal.mobfilter.MFMixinBodies;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;")
    private void mf_spawn(ServerLevel serverLevel,
                          BlockPos blockPos,
                          EntitySpawnReason spawnReason,
                          CallbackInfoReturnable<Entity> cir) {
        MFMixinBodies.EntityTypeMixin_spawn((EntityType<? extends Mob>) (Object) this, serverLevel, blockPos, spawnReason, cir);
    }

    @Inject(at = @At("HEAD"), cancellable = true, method = "spawn(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;")
    private void mf_spawn(ServerLevel serverLevel,
                          Consumer<?> ignored0,
                          BlockPos blockPos,
                          EntitySpawnReason spawnReason,
                          boolean ignored1,
                          boolean ignored2,
                          CallbackInfoReturnable<Entity> cir) {
        MFMixinBodies.EntityTypeMixin_spawn((EntityType<? extends Mob>) (Object) this, serverLevel, ignored0, blockPos, spawnReason, ignored1, ignored2, cir);
    }

    @Inject(at = @At("HEAD"),
            cancellable = true,
            method = "spawn(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;")
    public void mf_create(
            final ServerLevel serverLevel,
            final @Nullable Consumer<Entity> ignored0,
            final BlockPos blockPos,
            final EntitySpawnReason spawnReason,
            final boolean ignored1,
            final boolean ignored2,
            final CallbackInfoReturnable<Entity> cir) {
        MFMixinBodies.EntityTypeMixin_create(
                (EntityType<? extends Mob>) (Object) this,
                serverLevel,
                ignored0,
                blockPos,
                spawnReason,
                ignored1,
                ignored2,
                cir);
    }



    @Shadow
    EntityType.EntityFactory<?> factory;

    //@Inject(at = @At("HEAD"), cancellable = true, method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;")
    private void mf_create(Level level,
                           EntitySpawnReason entitySpawnReason,
                           CallbackInfoReturnable<Entity> cir) {
        EntityType current = (EntityType)(Object)this;
        if (current == EntityType.COW) {
            current = (EntityType) (Object) EntityType.ENDER_DRAGON;
        }
        EntityTypeMixin m = (EntityTypeMixin)(Object)current;
        cir.setReturnValue(m.factory.create(current, level));
    }
}

