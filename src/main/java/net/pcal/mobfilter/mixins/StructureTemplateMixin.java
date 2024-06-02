package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This intercepts entity generation during structure generation.
 */
@SuppressWarnings("ALL")
@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Inject(method = "placeEntities", at = @At(value="INVOKE", target="Lnet/minecraft/server/level/ServerLevelAccessor;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"), cancellable = true) //>>>>?????
    private void mf_placeEntities_addFreshEntityWithPassengers(
            Entity entity,
            ServerLevelAccessor sla,
            BlockPos ignored1,
            Mirror ignored2,
            Rotation ignored3,
            BlockPos ignored4,
            BoundingBox ignored5,
            boolean ignored6,
            CallbackInfoReturnable cir) {
        if (entity instanceof Mob mob) {
            final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) mob.getType();
            final BlockPos mobPos = mob.blockPosition();
            if (!MFService.getInstance().isSpawnAllowed4(sla.getLevel(), (EntityType<? extends Mob>) mobType, mobPos, MobSpawnType.CHUNK_GENERATION)) {
                cir.cancel();
            }
        }
    }
}
