package net.pcal.mobfilter.forge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;

@SuppressWarnings("ALL")
@Mixin(NaturalSpawner.SpawnState.class)
public class NaturalSpawnerSpawnStateMixin {

    /**
     * Ensures that filtered mobs don't count against mob caps or trigger
     * other finalization work.
     */
    @Inject(method = "afterSpawn(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/chunk/ChunkAccess;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void mf_afterSpawn(Mob mob, ChunkAccess chunkAccess, CallbackInfo ci) {
        if (mob.isRemoved()) ci.cancel();
    }
}

