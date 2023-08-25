package net.pcal.mobfilter;


import net.minecraft.entity.SpawnGroup;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

/**
 * Object model for the config file.
 */
@SuppressWarnings("ALL")
public class MFConfig {

    static ConfigurationFile load(final InputStream inputStream) {
        final LoaderOptions lopt = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(ConfigurationFile.class, lopt));
        return yaml.load(inputStream);
    }

    public static class ConfigurationFile {
        public String logLevel;
        public Rule[] rules;
    }

    public static class Rule {
        public String name;
        public MFRules.FilterRuleAction what;
        public When when;
    }

    public static class When {
        public String[] worldName;
        public String[] dimensionId;
        public String[] entityId;
        public String[] biomeId;
        public SpawnGroup[] spawnGroup;
        public String[] blockX;
        public String[] blockY;
        public String[] blockZ;
        public String[] blockId;
        public String[] timeOfDay;
        public String[] lightLevel;
    }
}

