package net.pcal.mobfilter.forge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.pcal.mobfilter.forge.ForgeMixinHandlers;

@SuppressWarnings("ALL")
@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"), cancellable = true, remap = false)
    private void mf_create(Level level, EntitySpawnReason entitySpawnReason, CallbackInfoReturnable<Entity> cir) {
        ForgeMixinHandlers.get().EntityType_create(level, entitySpawnReason, cir);
    }
}

