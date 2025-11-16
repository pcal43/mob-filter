package net.pcal.mobfilter;

import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Implementation of SpawnAttempt for the world generation thread.  Because we cannot safely access the world
 * state during worldgen, many of the attributes here must return null.
 */
public final class WorldgenThreadSpawnAttempt implements SpawnAttempt {

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
    public MinecraftId getEntityId() {
        return CommonMinecraftId.of(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
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
    public Integer getBlockX() {
        return this.blockPos.getX();
    }

    @Override
    public Integer getBlockY() {
        return this.blockPos.getY();
    }

    @Override
    public Integer getBlockZ() {
        return this.blockPos.getZ();
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
    public MinecraftId getDimensionId() {
        this.logger.debug(() -> "[MobFilter] dimensionId cannot be evaluated during world generation");
        return null;
    }

    @Override
    public MinecraftId getBlockId() {
        this.logger.debug(() -> "[MobFilter] blockId cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Integer getMoonPhase() {
        this.logger.debug(() -> "[MobFilter] moonPhase cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Integer getSkylightLevel() {
        this.logger.debug(() -> "[MobFilter] skyLightLevel cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Integer getLightLevel() {
        this.logger.debug(() -> "[MobFilter] lightLevel cannot be evaluated during world generation");
        return null;
    }

    @Override
    public WeatherType getWeather() {
        this.logger.debug(() -> "[MobFilter] weather cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Difficulty getDifficulty() {
        this.logger.debug(() -> "[MobFilter] difficulty cannot be evaluated during world generation");
        return null;
    }

    @Override
    public MinecraftId getBiomeId() {
        this.logger.debug("[MobFilter] biomeId cannot be evaluated during world generation");
        return null;
    }

    @Override
    public Long getDayTime() {
        this.logger.debug(() -> "[MobFilter] dayTime cannot be evaluated during world generation");
        return null;
    }
}

