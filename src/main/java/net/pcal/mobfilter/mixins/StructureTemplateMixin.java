package net.pcal.mobfilter.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.pcal.mobfilter.MFService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This intercepts entity generation during structure generation.
 */
@SuppressWarnings("ALL")
@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {


    // access flags 0x100A
    private static void method_17917(net.minecraft.world.level.block.Rotation ignored0,
                                     net.minecraft.world.level.block.Mirror ignored1,
                                     net.minecraft.world.phys.Vec3 ignored2,
                                     boolean ignored3,
                                     net.minecraft.world.level.ServerLevelAccessor sla,
                                     net.minecraft.world.entity.Entity entity
                                     ) {
        /**
         if (entity instanceof Mob mob) {
         final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) mob.getType();
         if (MFService.getInstance().isSpawnAllowed4(sla.getLevel(), (EntityType<? extends Mob>) mobType, mob.blockPosition(), MobSpawnType.CHUNK_GENERATION)) {
         sla.addFreshEntityWithPassengers(entity);
         }
         }**/
   }

    @Redirect(//method = "placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;net/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Z)V",
            method = "placeEntities",
            at = @At(value="INVOKE", ordinal=0,
                    target="Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void mf_placeEntities_addFreshEntityWithPassengers(
            net.minecraft.world.level.ServerLevelAccessor sla, // this is the instance that addFreshEntityWithPassengers is being called on
            Entity entity
            /**            // params from enclosing scope
            net.minecraft.world.level.ServerLevelAccessor ignored0,
            net.minecraft.core.BlockPos ignored1,
            net.minecraft.world.level.block.Mirror ignored2,
            net.minecraft.world.level.block.Rotation ignored3,
            net.minecraft.core.BlockPos ignored4,
            net.minecraft.world.level.levelgen.structure.BoundingBox ignored5,
            boolean ignored6**/
            ) {
        sla.addFreshEntityWithPassengers(entity);
        /**
         if (entity instanceof Mob mob) {
         final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) mob.getType();
         if (MFService.getInstance().isSpawnAllowed4(sla.getLevel(), (EntityType<? extends Mob>) mobType, mob.blockPosition(), MobSpawnType.CHUNK_GENERATION)) {
         sla.addFreshEntityWithPassengers(entity);
         }
         }**/
    }
}
