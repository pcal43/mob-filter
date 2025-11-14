package net.pcal.mobfilter.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import net.pcal.mobfilter.SpawnAttempt;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of SpawnAttempt for the main game thread.  All attributes of the world are available.
 */
public class MainThreadSpawnAttempt implements SpawnAttempt {

    private final ServerLevel serverWorld;
    private final EntitySpawnReason spawnReason;
    private final MobCategory category;
    private final EntityType<?> entityType;
    private final BlockPos blockPos;
    private final Logger logger;

    public MainThreadSpawnAttempt(final ServerLevel serverWorld,
                                  final EntitySpawnReason spawnReason,
                                  final MobCategory category,
                                  final EntityType<?> entityType,
                                  final BlockPos blockPos,
                                  final Logger logger) {
        this.serverWorld = requireNonNull(serverWorld);
        this.spawnReason = spawnReason;
        this.category = category;
        this.entityType = entityType;
        this.blockPos = blockPos;
        this.logger = requireNonNull(logger);
    }

    @Override
    public ResourceLocation getEntityId() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType); // FIXME is this right?
    }

    @Override
    public EntityType<?> getEntityType() {
        return this.entityType;
    }

    @Override
    public String getWorldName() {
        final ServerLevelData swp;
        try {
            swp = (ServerLevelData) this.serverWorld.getLevelData();
        } catch (ClassCastException cce) {
            this.logger.warn("[MobFilter] serverWorld.getLevelProperties() is unexpected class: " +
                    this.serverWorld.getLevelData().getClass().getName());
            return null;
        }
        return swp.getLevelName();
    }

    @Override
    public ResourceLocation getDimensionId() {
        return this.serverWorld.dimension().location();
    }

    @Override
    public ResourceLocation getBlockId() {
        final BlockState bs = serverWorld.getBlockState(this.blockPos.below());
        final Block block = bs.getBlock();
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    @Override
    public EntitySpawnReason getSpawnReason() {
        return spawnReason;
    }

    @Override
    public MobCategory getMobCategory() {
        return category;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public Integer getMoonPhase() {
        return serverWorld.getMoonPhase();
    }

    @Override
    public Integer getBrightness(LightLayer lightLayer, BlockPos blockPos) {
        return serverWorld.getBrightness(lightLayer, blockPos);
    }

    @Override
    public Integer getMaxLocalRawBrightness(BlockPos blockPos) {
        return serverWorld.getMaxLocalRawBrightness(blockPos);
    }

    @Override
    public Boolean isThundering() {
        return serverWorld.isThundering();
    }

    @Override
    public Boolean isRainingAt(BlockPos blockPos) {
        return serverWorld.isRainingAt(blockPos);
    }

    @Override
    public Difficulty getDifficulty() {
        return serverWorld.getDifficulty();
    }

    @Override
    public Biome getBiome(BlockPos blockPos) {
        final Holder<Biome> holder = serverWorld.getBiome(this.blockPos);
        //noinspection ConstantValue
        if (holder == null) {
            this.logger.debug(() -> "[MobFilter] null biome returned at " + this.blockPos);
            return null;
        } else {
            return holder.value();
        }
    }

    @Override
    public ResourceLocation getBiomeId() {
        final Biome biome = this.getBiome(blockPos);
        if (biome == null) return null;
        // FIXME? I'm not entirely sure this is correct
        return serverWorld.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
    }

    @Override
    public Long getDayTime() {
        return serverWorld.getDayTime();
    }
}
