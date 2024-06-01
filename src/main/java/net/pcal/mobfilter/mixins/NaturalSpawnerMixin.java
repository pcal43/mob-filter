package net.pcal.mobfilter.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.pcal.mobfilter.MFService;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    /**
     * Seems to be called when checking for a ranom spawn.
     */
    @Inject(method = "isValidSpawnPostitionForType(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;Lnet/minecraft/core/BlockPos$MutableBlockPos;D)Z", at = @At("RETURN"), cancellable = true)
    private static void mf_isValidSpawnPostitionForType(ServerLevel sw,
                                                        MobCategory sg,
                                                        StructureManager sa,
                                                        ChunkGenerator cg,
                                                        MobSpawnSettings.SpawnerData se,
                                                        BlockPos.MutableBlockPos pos,
                                                        double sd,
                                                        CallbackInfoReturnable<Boolean> ret) {
        if (ret.getReturnValue() == true) { // if minecraft code decided it canSpawn...
            // ...call our service to decide if we want to veto the spawn
            final boolean isSpawnAllowed = MFService.getInstance().isRandomSpawnAllowed(sw, sg, se, pos);
            // and change the return value if so
            if (!isSpawnAllowed) ret.setReturnValue(false);
        }
    }

    private static void isValidPositionForMob(net.minecraft.server.level.ServerLevel world,
                                                 net.minecraft.world.entity.Mob mob,
                                                 double d,
                                                 CallbackInfoReturnable<Boolean> ret) {
        if (ret.getReturnValue() == true) {
            final EntityType et = mob.getType();
            final BlockPos pos = mob.getPosition(d);
            final boolean isSpawnAllowed = MFService.getInstance().isWorldgenSpawnAllowed(world, pos, et);
            if (!isSpawnAllowed) ret.setReturnValue(false);
        }
    }

    public static boolean isValidEmptySpawnBlock(net.minecraft.world.level.BlockGetter blockGetter, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.material.FluidState fluidState, net.minecraft.world.entity.EntityType<?> entityType) {

        /* compiled code */
    }
    private boolean canSpawn(net.minecraft.world.entity.EntityType<?> entityType, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.chunk.ChunkAccess chunkAccess) {
        entityType.get
    }

    /**
     * Seems to be called when checking for a spawn during worldgen.
     */
    @Inject(method = "isSpawnPositionOk(Lnet/minecraft/world/entity/SpawnPlacements$Type;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z", at = @At("RETURN"), cancellable = true)
    private static void mf_isSpawnPositionOk(SpawnPlacementType ignored,
                                             LevelReader world,
                                             BlockPos pos,
                                             @Nullable EntityType<?> et,
                                             CallbackInfoReturnable<Boolean> ret) {
        if (ret.getReturnValue() == true) {
            final boolean isSpawnAllowed = MFService.getInstance().isWorldgenSpawnAllowed(world, pos, et);
            if (!isSpawnAllowed) ret.setReturnValue(false);
        }
    }
}
