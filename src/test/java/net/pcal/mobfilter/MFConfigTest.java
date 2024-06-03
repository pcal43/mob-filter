package net.pcal.mobfilter;

import net.minecraft.world.entity.MobCategory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MFConfigTest {

    @Test
    public void testHelloWorld() throws Exception {
        final MFConfig.ConfigurationFile config;
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-config.yaml")) {
            config = MFConfig.load(inputStream);
        }
        assertEquals("TRACE", config.logLevel);
        assertEquals(2, config.rules.length);
        assertEquals(MobCategory.MONSTER, config.rules[1].when.spawnGroup[0]);
    }
}
