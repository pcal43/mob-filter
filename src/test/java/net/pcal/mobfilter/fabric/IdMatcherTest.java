package net.pcal.mobfilter.fabric;

import net.minecraft.resources.ResourceLocation;
import net.pcal.mobfilter.IdMatcher;
import net.pcal.mobfilter.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdMatcherTest {

    @Test
    public void testMatching() {

        final Platform platform = FabricPlatform.get();

        Assertions.assertFalse(IdMatcher.of(new String[] {}).isMatch(r("minecraft:cobblestone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone" }).isMatch(r("minecraft:cobblestone")));
        assertFalse(IdMatcher.of(new String[] { "minecraft:cobblestone" }).isMatch(r("minecraft:stone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:*" }).isMatch(r("minecraft:cobblestone")));
        assertTrue(IdMatcher.of(new String[] { "minecraft:*" }).isMatch(r("minecraft:stone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }).isMatch(r("minecraft:cobblestone")));
        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }).isMatch(r("mymod:magicblock")));
        assertFalse(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }).isMatch(r("minecraft:redstone")));
    }

    private static ResourceLocation r(String val) {
        return ResourceLocation.parse(val);
    }


}