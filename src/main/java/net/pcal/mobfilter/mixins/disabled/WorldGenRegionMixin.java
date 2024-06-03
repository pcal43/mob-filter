package net.pcal.mobfilter.mixins.disabled;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catches entity creation during chunk generation, mainly from StructureTemplate#placeEntities.
 *
 * DISABLED.  This is a very low-level, last-minute intercept all entity additions.  This is hopefully redundant
 * (we try to catch spawn events earlier in the other mx with earlier checks) and may have unanticipated side effects,
 * so disabled in mixins.json.  Leaving it here as a note.
  */
@SuppressWarnings("ALL")
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void mf_addFreshEntity(Entity entity, CallbackInfoReturnable ci) {
        if (!MFService.getInstance().isSpawnAllowed(
                ((WorldGenRegion)(Object)this).getLevel(),
                MobSpawnType.CHUNK_GENERATION, (EntityType<Mob>)entity.getType(),
                entity.blockPosition()
        )) { // they aren't techwe can reasonably classify these
            ci.cancel();
        }
    }
}
