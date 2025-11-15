package net.pcal.mobfilter.forge;

import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import net.pcal.mobfilter.MinecraftId;
import net.pcal.mobfilter.SpawnAttempt;
import net.pcal.mobfilter.WeatherType;
import static net.pcal.mobfilter.forge.ForgePlatform.id;

/**
 * Implementation of SpawnAttempt for the main game thread.  All attributes of the world are available.
 */
public final class MainThreadSpawnAttempt implements SpawnAttempt {

    private final ServerLevel serverWorld;
    private final MobSpawnType spawnReason;
    private final MobCategory category;
    private final EntityType<?> entityType;
    private final BlockPos blockPos;
    private final Logger logger;

    public MainThreadSpawnAttempt(final ServerLevel serverWorld,
                                  final MobSpawnType spawnReason,
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
    public MinecraftId getEntityId() {
        return id(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)); // FIXME is this right?
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
    public MinecraftId getDimensionId() {
        return id(this.serverWorld.dimension().location());
    }

    @Override
    public MinecraftId getBlockId() {
        final BlockState bs = serverWorld.getBlockState(this.blockPos.below());
        final Block block = bs.getBlock();
        return id(BuiltInRegistries.BLOCK.getKey(block));
    }

    @Override
    public MobSpawnType getSpawnReason() {
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
    public Integer getMoonPhase() {
        return serverWorld.getMoonPhase();
    }

    @Override
    public Integer getSkylightLevel() {
        return serverWorld.getBrightness(LightLayer.SKY, blockPos);
    }

    @Override
    public Integer getLightLevel() {
        return serverWorld.getMaxLocalRawBrightness(blockPos);
    }

    @Override
    public Difficulty getDifficulty() {
        return serverWorld.getDifficulty();
    }

    @Override
    public MinecraftId getBiomeId() {
        final Holder<Biome> holder = serverWorld.getBiome(this.blockPos);
        if (holder == null) return null;
        return holder.unwrapKey()
                .map(key -> id(key.location()))
                .orElse(null);
    }

    @Override
    public WeatherType getWeather() {
        if (blockPos == null) {
            getLogger().debug(() -> "[MobFilter] WeatherCheck: no block position");
            return null;
        }
        if (serverWorld.isThundering()) return WeatherType.THUNDER;
        if (serverWorld.isRainingAt(blockPos)) {
            // Check for snow
            final Biome biome = getBiome();
            if (biome == null) {
                getLogger().debug(() -> "[MobFilter] WeatherCheck: biome could not be determined");
                return null;
            } else if (biome.hasPrecipitation() && biome.coldEnoughToSnow(blockPos)) {
                return WeatherType.SNOW;
            } else {
                return WeatherType.RAIN;
            }
        }
        return WeatherType.CLEAR;
    }

    @Override
    public Long getDayTime() {
        return serverWorld.getDayTime();
    }


    // ===================================================================================
    // Private

    private Biome getBiome() {
        final Holder<Biome> holder = serverWorld.getBiome(this.blockPos);
        //noinspection ConstantValue
        if (holder == null) {
            this.logger.debug(() -> "[MobFilter] null biome returned at " + this.blockPos);
            return null;
        } else {
            return holder.value();
        }
    }
}

