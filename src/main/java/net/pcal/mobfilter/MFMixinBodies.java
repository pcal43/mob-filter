package net.pcal.mobfilter;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * Raw implementations of our mixin methods.  Putting them here just makes them more debuggable/reloadable.
 */
@SuppressWarnings("unchecked")
public class MFMixinBodies {

    public static void EntityTypeMixin_spawn(EntityType<? extends Mob> self,
                                             ServerLevel serverLevel,
                                             BlockPos blockPos,
                                             MobSpawnType mobSpawnType,
                                             CallbackInfoReturnable<Entity> cir) {
        if (!MFService.getInstance().isSpawnAllowed(serverLevel, mobSpawnType, self, blockPos)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

    public static void EntityTypeMixin_spawn(EntityType<? extends Mob> self, ServerLevel serverLevel,
                                             Consumer<?> ignored0,
                                             BlockPos blockPos,
                                             MobSpawnType mobSpawnType,
                                             boolean ignored1,
                                             boolean ignored2,
                                             CallbackInfoReturnable<Entity> cir) {
        if (!MFService.getInstance().isSpawnAllowed(serverLevel, mobSpawnType, self, blockPos)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

    public static void SpawnPlacementsMixin_checkSpawnRules(EntityType<?> entityType,
                                                            ServerLevelAccessor sla,
                                                            MobSpawnType mobSpawnType,
                                                            BlockPos blockPos,
                                                            RandomSource ignored,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), mobSpawnType, (EntityType<? extends Mob>) entityType, blockPos)) {
            cir.setReturnValue(false);
        }
    }

    public static void StructureTemplateMixin_method_17917(Rotation ignored0,
                                                           Mirror ignored1,
                                                           Vec3 ignored2,
                                                           boolean ignored3,
                                                           ServerLevelAccessor sla,
                                                           Entity entity,
                                                           CallbackInfo ci
    ) {
        if (entity instanceof Mob mob) {
            final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) mob.getType();
            if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), MobSpawnType.STRUCTURE, mobType, mob.blockPosition())) {
                ci.cancel();
            }
        }
    }
}
