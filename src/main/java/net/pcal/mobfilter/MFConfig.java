package net.pcal.mobfilter;


import com.google.common.collect.ImmutableList;
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
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.Rule.RuleAction;
import net.pcal.mobfilter.RuleCheck.BiomeCheck;
import net.pcal.mobfilter.RuleCheck.BlockIdCheck;
import net.pcal.mobfilter.RuleCheck.BlockPosCheck;
import net.pcal.mobfilter.RuleCheck.CategoryCheck;
import net.pcal.mobfilter.RuleCheck.DimensionCheck;
import net.pcal.mobfilter.RuleCheck.EntityIdCheck;
import net.pcal.mobfilter.RuleCheck.LightLevelCheck;
import net.pcal.mobfilter.RuleCheck.MoonPhaseCheck;
import net.pcal.mobfilter.RuleCheck.RandomCheck;
import net.pcal.mobfilter.RuleCheck.SkylightLevelCheck;
import net.pcal.mobfilter.RuleCheck.SpawnReasonCheck;
import net.pcal.mobfilter.RuleCheck.TimeOfDayCheck;
import net.pcal.mobfilter.RuleCheck.WeatherCheck;
import net.pcal.mobfilter.RuleCheck.WeatherType;
import net.pcal.mobfilter.RuleCheck.WorldNameCheck;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;

import static java.util.Objects.requireNonNull;

/**
 * Object model for the config file.
 */
@SuppressWarnings("ALL")
public class MFConfig {

    /**
     * Build the runtime rule structures from the configuration.  Returns null if the configuration contains
     * no rules.
     */
    static void loadRules(final InputStream in, final RuleList.Builder configBuilder) throws IOException {
        final JsonConfiguration fromConfig = loadFromJson(in);
        if (fromConfig != null && fromConfig.rules != null) {
            loadRules(fromConfig ,configBuilder);
        }
    }

    static void loadRules(final JsonConfiguration fromConfig, final RuleList.Builder configBuilder) {
        int i = -1;
        for (final JsonRule configRule : fromConfig.rules) {
            i++;
            if (configRule == null) continue; // common with json trailing comma in list
            final ImmutableList.Builder<RuleCheck> checks = ImmutableList.builder();
            final String ruleName = configRule.name != null ? configRule.name : "rule" + i;
            if (configRule.what == null) {
                throw new IllegalArgumentException("'what' must be specified on " + ruleName);
            }
            final JsonWhen when = configRule.when;
            if (when == null) {
                throw new IllegalArgumentException("'when' must be specified on " + ruleName);
            }
            if (when.spawnReason != null && when.spawnReason.length > 0) {
                final EnumSet<EntitySpawnReason> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnReason));
                checks.add(new SpawnReasonCheck(enumSet));
            } else if (when.spawnType != null && when.spawnType.length > 0) {
                // legacy support for old name 'spawnType'
                final EnumSet<EntitySpawnReason> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnType));
                checks.add(new SpawnReasonCheck(enumSet));
            }
            if (when.category != null && when.category.length > 0) {
                final EnumSet<MobCategory> enumSet = EnumSet.copyOf(Arrays.asList(when.category));
                checks.add(new CategoryCheck(enumSet));
            } else if (when.spawnGroup != null && when.spawnGroup.length > 0) {
                // legacy support for old name 'spawnGroup'
                final EnumSet<MobCategory> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnGroup));
                checks.add(new CategoryCheck(enumSet));
            }
            if (when.entityId != null) {
                checks.add(new EntityIdCheck(IdMatcher.of(when.entityId)));
            }
            if (when.worldName != null) {
                checks.add(new WorldNameCheck(Matcher.of(when.worldName)));
            }
            if (when.dimensionId != null) {
                checks.add(new DimensionCheck(IdMatcher.of(when.dimensionId)));
            }
            if (when.biomeId != null) {
                checks.add(new BiomeCheck(IdMatcher.of(when.biomeId)));
            }
            if (when.blockId != null) {
                checks.add(new BlockIdCheck(IdMatcher.of(when.blockId)));
            }
            if (when.blockX != null) {
                int[] range = parseRange(when.blockX);
                checks.add(new BlockPosCheck(Direction.Axis.X, range[0], range[1]));
            }
            if (when.blockY != null) {
                int[] range = parseRange(when.blockY);
                checks.add(new BlockPosCheck(Direction.Axis.Y, range[0], range[1]));
            }
            if (when.blockZ != null) {
                int[] range = parseRange(when.blockZ);
                checks.add(new BlockPosCheck(Direction.Axis.Z, range[0], range[1]));
            }
            if (when.timeOfDay != null) {
                int[] range = parseRange(when.timeOfDay);
                checks.add(new TimeOfDayCheck(range[0], range[1]));
            }
            if (when.lightLevel != null) {
                int[] range = parseRange(when.lightLevel);
                checks.add(new LightLevelCheck(range[0], range[1]));
            }
            if (when.skylightLevel != null) {
                int[] range = parseRange(when.skylightLevel);
                checks.add(new SkylightLevelCheck(range[0], range[1]));
            }
            if (when.moonPhase != null) {
                checks.add(new MoonPhaseCheck(Matcher.of(when.moonPhase)));
            }
            if (when.weather != null) {
                checks.add(new WeatherCheck(Matcher.of(when.weather)));
            }
            if (when.random != null) {
                checks.add(new RandomCheck(when.random));
            }
            configBuilder.addRule(new net.pcal.mobfilter.Rule(ruleName, checks.build(), configRule.what));
        }
        if (fromConfig.logLevel != null) {
            try {
                configBuilder.setLogLevel(Level.getLevel(fromConfig.logLevel));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid logLevel value: " + fromConfig.logLevel, e);
            }
        }
    }

    static JsonConfiguration loadFromJson(final InputStream in) throws IOException {
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
        return gson.fromJson(new TypoCatchingJsonReader(new StringReader(rawJson)), TypeToken.get(JsonConfiguration.class));
    }

    /**
     * Parse a two-value list into an integer range.
     */
    private static int[] parseRange(String[] configValues) {
        if (configValues.length != 2) {
            throw new IllegalArgumentException("Invalid number of values in int range: " + Arrays.toString(configValues));
        }
        int[] out = new int[2];
        out[0] = "MIN".equals(configValues[0]) ? Integer.MIN_VALUE : Integer.parseInt(configValues[0]);
        out[1] = "MAX".equals(configValues[1]) ? Integer.MAX_VALUE : Integer.parseInt(configValues[1]);
        if (out[0] > out[1]) {
            throw new IllegalArgumentException("Invalid min/max range: " + Arrays.toString(configValues));
        }
        return out;
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

    public static class JsonConfiguration {
        public JsonRule[] rules;
        public String logLevel;
    }

    public static class JsonRule {
        public String name;
        public RuleAction what;
        public JsonWhen when;
    }

    public static class JsonWhen {
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
