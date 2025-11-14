package net.pcal.mobfilter.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.pcal.mobfilter.SpawnAttempt;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of SpawnAttempt for the world generation thread.  Because we cannot safely access the world
 * state during worldgen, many of the attributes here must return null.
 */
public class WorldgenThreadSpawnAttempt implements SpawnAttempt {

    private final EntitySpawnReason spawnReason;
    private final MobCategory category;
    private final EntityType<?> entityType;
    private final BlockPos blockPos;
    private final Logger logger;

    public WorldgenThreadSpawnAttempt(final EntitySpawnReason spawnReason,
                                      final MobCategory category,
                                      final EntityType<?> entityType,
                                      final BlockPos blockPos,
                                      final Logger logger) {
        this.spawnReason = spawnReason;
        this.category = category;
        this.entityType = entityType;
        this.blockPos = blockPos;
        this.logger = requireNonNull(logger);
    }

    /**
     * Return the entity id of the mob that is going to spawn.
     */
    @Override
    public ResourceLocation getEntityId() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType); // FIXME is this right?
    }

    /**
     * Return the entity id of the mob that is going to spawn.
     */
    public EntityType<?> getEntityId() {
        return this.entityType;
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
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    // ======================================================================
    // Unsupported during world generation

    @Override
    public String getWorldName() {
        this.logger.debug(() -> "[MobFilter] worldName cannot be evaluated during world generation");
        return null;
    }

    @Override
    public ResourceLocation getDimensionId() {
        this.logger.debug(() -> "[MobFilter] dimensionId cannot be evaluated during world generation");
        return null;
    }

    @Override
    public ResourceLocation getBlockId() {
        this.logger.debug(() -> "[MobFilter] blockId cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Integer getMoonPhase() {
        this.logger.debug(() -> "[MobFilter] moonPhase cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Integer getBrightness(LightLayer lightLayer, BlockPos blockPos) {
        this.logger.debug(() -> "[MobFilter] brightness cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Integer getLightLevel(BlockPos blockPos) {
        this.logger.debug(() -> "[MobFilter] maxLocalRawBrightness cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Boolean isThundering() {
        this.logger.debug(() -> "[MobFilter] thundering cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Boolean isRainingAt(BlockPos blockPos) {
        this.logger.debug(() -> "[MobFilter] isRainingAt cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Difficulty getDifficulty() {
        this.logger.debug(() -> "[MobFilter] difficulty cannot be evaluated during world generation");
        return null;
    }

    @Override
    public ResourceLocation getBiomeId() {
        this.logger.debug("[MobFilter] biomeId cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Biome getBiome(BlockPos blockPos) {
        this.logger.debug(() -> "[MobFilter] biome cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Long getDayTime() {
        this.logger.debug(() -> "[MobFilter] dayTime cannot be evaluated during world generation");
        return null;
    }
}
