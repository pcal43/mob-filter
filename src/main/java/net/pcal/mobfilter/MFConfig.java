package net.pcal.mobfilter;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.Rule.RuleAction;
import net.pcal.mobfilter.RuleCheck.WeatherType;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Object model for the config file.
 */
@SuppressWarnings("ALL")
public class MFConfig {

    static Configuration loadFromJson(final InputStream in) throws IOException {
        final String rawJson = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        final Gson gson = new GsonBuilder().
                setLenient().
                registerTypeAdapterFactory(new ValidatingEnumAdapterFactory()).
                create();
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
                throw new RuntimeException("Unexpected configuration names at: "+this.toString());
            }

            /**
             * The base class doesn't expose useful info like line number except via toString().
             * Hack it up to make it a little less ugly.
             */
            @Override
            public String toString() {
                String out = super.toString();
                String possiblePrefix = getClass().getSimpleName() + " at ";
                if (out.startsWith(possiblePrefix)) out = out.substring(possiblePrefix.length());
                return out;
            }
        }
        return gson.fromJson(new TypoCatchingJsonReader(new StringReader(rawJson)), TypeToken.get(Configuration.class));
    }

    /**
     * By default, invalid enum values in the json file silently get bound by gson as null.
     * Which ends up causing pain and confusion later.  This adapter fails loudly and
     * clearly instead.
     */
    private static class ValidatingEnumAdapterFactory implements TypeAdapterFactory {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!type.getRawType().isEnum()) return null; // Only handle enums
            final TypeAdapter<T> defaultAdapter = gson.getDelegateAdapter(this, type);
            final Class<T> enumClass = (Class<T>) type.getRawType();
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    defaultAdapter.write(out, value);
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    String value = in.nextString();
                    if (Arrays.stream(enumClass.getEnumConstants())
                            .noneMatch(e -> ((Enum<?>) e).name().equals(value))) {
                        final StringBuilder msg = new StringBuilder();
                        msg.append("Invalid "+enumClass.getSimpleName()+" value '"+value+"' at " + in.toString());
                        msg.append(".  Valid values are: ");
                        boolean isFirst = true;
                        for (T val : enumClass.getEnumConstants()) {
                            if (isFirst) isFirst = false; else msg.append(", ");
                            msg.append(val);
                        }
                        throw new JsonParseException(msg.toString());
                    }
                    return defaultAdapter.fromJsonTree(new JsonPrimitive(value));
                }
            };
        }
    }

    public static class Configuration {
        public Rule[] rules;
        public String logLevel;
    }

    public static class Rule {
        public String name;
        public RuleAction what;
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
        public String[] skylightLevel;
        public Integer[] moonPhase;
        public WeatherType[] weather;
        public Double random;

        // for backwards compatibility:
        @Deprecated // use spawnReason instead
        public EntitySpawnReason[] spawnType;
        @Deprecated // use category instead
        public MobCategory[] spawnGroup;
    }
}
