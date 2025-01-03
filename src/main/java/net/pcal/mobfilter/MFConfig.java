package net.pcal.mobfilter;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Object model for the config file.
 */
@SuppressWarnings("ALL")
public class MFConfig {

    static Configuration loadFromJson(final InputStream in) throws IOException {
        final String rawJson = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        final Gson gson = new GsonBuilder().setLenient().create();
        class TypoCatchingJsonReader extends JsonReader {
            public TypoCatchingJsonReader(StringReader in) {
                super(in);
                super.setStrictness(Strictness.LENIENT);
            }

            @Override
            public void skipValue()  {
                // GSon calls this to silently ignore json keys that don't bind to anything.  People then get
                // confused about why their configuration isn't fully working.  So here we just fail loudly instead.
                // Note we don't throw IOException because GSon tries to handle that in a waysthat obscures the message.
                throw new RuntimeException("Unexpected configuration names at: "+this.getPath());
            }
        }
        return gson.fromJson(new TypoCatchingJsonReader(new StringReader(rawJson)), TypeToken.get(Configuration.class));
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
        public EntitySpawnReason[] spawnReason;
        public MobCategory[] category;
        public String[] blockX;
        public String[] blockY;
        public String[] blockZ;
        public String[] blockId;
        public String[] timeOfDay;
        public String[] lightLevel;
        public Integer[] moonPhase;

        // for backwards compatibility:
        @Deprecated // use spawnReason instead
        public EntitySpawnReason[] spawnType;
        @Deprecated // use category instead
        public MobCategory[] spawnGroup;
    }
}
