package net.pcal.mobfilter.fabric;

import net.minecraft.resources.ResourceLocation;
import net.pcal.mobfilter.MinecraftId;

public class FabricMinecraftId {

    public static MinecraftId id(ResourceLocation loc) {
        return new MinecraftId() {
            @Override
            public String getNamespace() {
                return loc.getNamespace();
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }
        };
    }

}
