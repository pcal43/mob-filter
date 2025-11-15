package net.pcal.mobfilter.forge.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.pcal.mobfilter.forge.ForgeMixinHandlers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(at = @At("RETURN"), method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;", cancellable = true)
    private void mf_create(Level level, MobSpawnType mobSpawnType, CallbackInfoReturnable<Entity> cir) {
        ForgeMixinHandlers.get().EntityType_create(level, mobSpawnType, cir);
    }
}

