package net.pcal.mobfilter;

import net.minecraft.resources.ResourceLocation;

/**
 * Common implementation of MinecraftId for use in the common module.
 */
record CommonMinecraftId(ResourceLocation loc) implements MinecraftId {

    @Override
    public String getNamespace() {
        return loc.getNamespace();
    }

    @Override
    public String toString() {
        return loc.toString();
    }

    /**
     * Factory method to create a MinecraftId from a ResourceLocation.
     */
    static MinecraftId of(ResourceLocation loc) {
        return new CommonMinecraftId(loc);
    }
}

