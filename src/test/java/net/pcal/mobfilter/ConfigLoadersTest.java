package net.pcal.mobfilter;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.JsonConfigLoader.JsonConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigLoadersTest {

    @SuppressWarnings({"deprecation", "DataFlowIssue"})
    @Test
    public void testJson() throws Exception {
        try (final InputStream in = requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("ConfigLoadersTest/testJson/test-config.json5"));
             final InputStream ex = requireNonNull(getClass().getClassLoader()
                     .getResourceAsStream("ConfigLoadersTest/testJson/config.expected"))) {
            final String expectedConfig = new String(ex.readAllBytes(), UTF_8);

            final JsonConfiguration jsonConfig = JsonConfigLoader.loadFromJson(in);
            assertEquals("TRACE", jsonConfig.logLevel);
            assertEquals(2, jsonConfig.rules.length);
            assertEquals(MobCategory.MONSTER, jsonConfig.rules[1].when.spawnGroup[0]);
            assertEquals(EntitySpawnReason.STRUCTURE, jsonConfig.rules[1].when.spawnReason[0]);
            assertEquals(EntitySpawnReason.JOCKEY, jsonConfig.rules[1].when.spawnType[0]);
            assertArrayEquals(new WeatherType[]{WeatherType.RAIN, WeatherType.THUNDER},
                    jsonConfig.rules[1].when.weather);
            assertArrayEquals(new String[]{"5", "10",}, jsonConfig.rules[1].when.lightLevel);
            assertArrayEquals(new String[]{"10", "20",}, jsonConfig.rules[1].when.skylightLevel);
            assertArrayEquals(new Integer[]{3, 4, 5}, jsonConfig.rules[1].when.moonPhase);
            assertEquals(0.45d, jsonConfig.rules[1].when.random);


            // kick tires on rule building
            final Config.Builder configBuilder = Config.builder();
            JsonConfigLoader.loadRules(jsonConfig, configBuilder);
            final Config modConfig = configBuilder.build();


            final String configString = configToString(modConfig);
            System.out.println(configString);
            assertEquals(expectedConfig, configString);
        }
    }

    @Test
    public void testJsonEmpty() throws Exception {
        final InputStream in = getClass().getClassLoader()
                .getResourceAsStream("ConfigLoadersTest/testJsonEmpty/empty-config.json5");
        final Config.Builder configBuilder = new Config.Builder();
        JsonConfigLoader.loadRules(in, configBuilder);
        final Config rules = configBuilder.build();
        final String configString = configToString(rules);
        System.out.println(configString);
        assertEquals("LogLevel: INFO\n", configString);
    }

    @Test
    public void testSimple() throws Exception {
        try (final InputStream in = requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("ConfigLoadersTest/testSimple/test-config.simple"));
             final InputStream ex = requireNonNull(getClass().getClassLoader()
                     .getResourceAsStream("ConfigLoadersTest/testSimple/config.expected"))) {
            final String expectedConfig = new String(ex.readAllBytes(), UTF_8);
            // Build rules using SimpleConfigLoader
            Config.Builder configBuilder = new Config.Builder();
            SimpleConfigLoader.loadRules(in, configBuilder);
            Config rules = configBuilder.build();

            final String configString = configToString(rules);
            System.out.println(configString);
            assertEquals(expectedConfig, configString);
        }
    }


    @Test
    public void testSimpleEmpty() throws Exception {
        final InputStream in = getClass().getClassLoader()
                .getResourceAsStream("ConfigLoadersTest/testSimpleEmpty/empty-config.simple");
        // Build rules using SimpleConfigLoader
        Config.Builder configBuilder = new Config.Builder();
        SimpleConfigLoader.loadRules(in, configBuilder);
        Config rules = configBuilder.build();

        final String configString = configToString(rules);
        System.out.println(configString);
        assertEquals("LogLevel: INFO\n", configString);
    }

    private static String configToString(Config config) {
        final StringBuilder sb = new StringBuilder();
        for (final Rule rule : config.getRules()) {
            sb.append(rule.toString()).append("\n");
        }
        sb.append("LogLevel: " + config.getLogLevel() + "\n");
        return sb.toString();
    }
}
