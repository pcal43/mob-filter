package net.pcal.mobfilter.mixins;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "getMobForSpawn")
    private static void getMobForSpawn(ServerLevel serverLevel, EntityType<?> entityType, CallbackInfoReturnable cir) {
        /**
        entityType = EntityType.ZOMBIE_HORSE;
        try {
            Entity var3 = entityType.create(serverLevel, EntitySpawnReason.NATURAL);
            if (var3 instanceof Mob mob) {
                cir.setReturnValue(var3);
            }

//            NaturalSpawner.LOGGER.warn("Can't spawn entity of type: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
        } catch (Exception exception) {
//            LOGGER.warn("Failed to create mob", exception);
        }
         **/
    }
}
