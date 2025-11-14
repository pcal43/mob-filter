package net.pcal.mobfilter.fabric;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.MinecraftId;
import net.pcal.mobfilter.Platform;

import static net.pcal.mobfilter.fabric.FabricMinecraftId.id;

public class FabricPlatform implements Platform {

    @Override
    public MinecraftId parseMinecraftId(String id) {
        return id(ResourceLocation.parse(id));
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
}
