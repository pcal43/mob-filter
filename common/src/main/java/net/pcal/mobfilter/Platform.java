package net.pcal.mobfilter;

import java.nio.file.Path;

public interface Platform {

    MinecraftId parseMinecraftId(String id);

    Path getConfigFilePath(String filename);

    Class<? extends Enum<?>> getSpawnReasonEnum();

    Class<? extends Enum<?>> getMobCategoryEnum();

    Class<? extends Enum<?>> getDifficultyEnum();
}
