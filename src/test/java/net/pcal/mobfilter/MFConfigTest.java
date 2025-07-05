package net.pcal.mobfilter;

import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.MFConfig.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MFConfigTest {

    @SuppressWarnings({"deprecation", "DataFlowIssue"})
    @Test
    public void testJson() throws Exception {
        // kick tires on json parsing
        final Configuration config;
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-config.json5")) {
            config = MFConfig.loadFromJson(inputStream);
        }
        assertEquals("TRACE", config.logLevel);
        assertEquals(2, config.rules.length);
        assertEquals(MobCategory.MONSTER, config.rules[1].when.spawnGroup[0]);
        assertEquals(MobSpawnType.STRUCTURE, config.rules[1].when.spawnType[0]);
        assertEquals(MobSpawnType.JOCKEY, config.rules[1].when.spawnType[0]);
        assertArrayEquals(new MFRules.WeatherType[] { MFRules.WeatherType.RAIN, MFRules.WeatherType.THUNDER},
                config.rules[1].when.weather);
        assertArrayEquals(new String[] { "5", "10", }, config.rules[1].when.lightLevel);
        assertArrayEquals(new String[] { "10", "20", }, config.rules[1].when.skylightLevel);
        assertArrayEquals(new Integer[] { 3, 4, 5 }, config.rules[1].when.moonPhase);
        assertEquals(0.45d, config.rules[1].when.random);


        // kick tires on rule building
        List<MFRules.FilterRule> rules = MFService.buildRules(config).getRules();
        assertEquals(2, rules.size());
        // the checks declared using deprecated names get ignored
        assertEquals(
                EnumSet.of(MobSpawnType.STRUCTURE),
                ((MFRules.SpawnTypeCheck)rules.get(1).checks().get(0)).types());
        // but if only the old name is present, we use it
        assertEquals(
                EnumSet.of(MobCategory.MONSTER),
                ((MFRules.CategoryCheck)rules.get(1).checks().get(1)).categories());
    }
}
