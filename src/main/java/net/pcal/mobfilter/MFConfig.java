package net.pcal.mobfilter;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Object model for the config file.
 */
@SuppressWarnings("ALL")
public class MFConfig {

    static Configuration loadFromYaml(final InputStream inputStream) {
        final LoaderOptions lopt = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(Configuration.class, lopt));
        return yaml.load(inputStream);
    }

    static Configuration loadFromJson(final InputStream in) throws IOException {
        final String rawJson = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        final Gson gson = new GsonBuilder().setLenient().create();  
        return gson.fromJson(rawJson, Configuration.class);
    }

    public static class Configuration {
        public Rule[] rules;
        public String logLevel;
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
        public MobSpawnType[] spawnType;
        @Deprecated // use category instead
        public MobCategory[] spawnGroup;
        public MobCategory[] category;
        public String[] blockX;
        public String[] blockY;
        public String[] blockZ;
        public String[] blockId;
        public String[] timeOfDay;
        public String[] lightLevel;
        public Integer[] moonPhase;
    }
}
