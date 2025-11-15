package net.pcal.mobfilter.fabric;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.MinecraftId;
import net.pcal.mobfilter.Platform;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FabricPlatform implements Platform {

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final FabricPlatform INSTANCE;

        static {
            INSTANCE = new FabricPlatform();
        }
    }

    public static FabricPlatform get() {
        return SingletonHolder.INSTANCE;
    }

    // ===================================================================================
    // Platform implementation

    @Override
    public MinecraftId parseMinecraftId(String id) {
        return new FabricMinecraftId(ResourceLocation.parse(id));
    }

    @Override
    public Path getConfigFilePath(String filename) {
        return Paths.get("config", filename);
    }

    @Override
    public Class<? extends Enum<?>> getSpawnReasonEnum() {
        return EntitySpawnReason.class;
    }

    @Override
    public Class<? extends Enum<?>> getMobCategoryEnum() {
        return MobCategory.class;
    }

    @Override
    public  Class<? extends Enum<?>> getDifficultyEnum() {
        return Difficulty.class;
    }


    // ===================================================================================
    // Package

    static MinecraftId id(ResourceLocation loc) {
        return new FabricMinecraftId(loc);
    }

    // ===================================================================================
    // Private

    private record FabricMinecraftId(ResourceLocation loc) implements MinecraftId {

        @Override
        public String getNamespace() {
            return loc.getNamespace();
        }

        @Override
        public String toString() {
            return loc.toString();
        }
    }
}
