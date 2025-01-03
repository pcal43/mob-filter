package net.pcal.mobfilter;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.MFConfig.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MFConfigTest {

    @Test
    public void testYaml() throws Exception {
        final Configuration config;
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-config.yaml")) {
            config = MFConfig.loadFromYaml(inputStream);
        }
        assertEquals("TRACE", config.logLevel);
        assertEquals(2, config.rules.length);
        assertEquals(MobCategory.MONSTER, config.rules[1].when.spawnGroup[0]);
    }


    @Test
    public void testJson() throws Exception {
        final Configuration config;
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-config.json5")) {
            config = MFConfig.loadFromJson(inputStream);
        }
        assertEquals("TRACE", config.logLevel);
        assertEquals(2, config.rules.length);
        assertEquals(MobCategory.MONSTER, config.rules[1].when.spawnGroup[0]);
        assertEquals(EntitySpawnReason.STRUCTURE, config.rules[1].when.spawnReason[0]);
        assertEquals(EntitySpawnReason.JOCKEY, config.rules[1].when.spawnType[0]);

        List<MFRules.FilterRule> rules = MFService.buildRules(config).getRules();
        assertEquals(2, rules.size());
        // the checks declared using deprecated names get ignored
        assertEquals(
                EnumSet.of(EntitySpawnReason.STRUCTURE),
                ((MFRules.SpawnReasonCheck)rules.get(1).checks().get(0)).reasons());
        // but if only the old name is present, we use it
        assertEquals(
                EnumSet.of(MobCategory.MONSTER),
                ((MFRules.CategoryCheck)rules.get(1).checks().get(1)).categories());




    }
}
