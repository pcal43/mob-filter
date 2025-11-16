package net.pcal.mobfilter;


import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.pcal.mobfilter.Rule.RuleAction;
import net.pcal.mobfilter.RuleCheck.BiomeCheck;
import net.pcal.mobfilter.RuleCheck.BlockIdCheck;
import net.pcal.mobfilter.RuleCheck.BlockXCheck;
import net.pcal.mobfilter.RuleCheck.BlockYCheck;
import net.pcal.mobfilter.RuleCheck.BlockZCheck;
import net.pcal.mobfilter.RuleCheck.CategoryCheck;
import net.pcal.mobfilter.RuleCheck.DifficultyCheck;
import net.pcal.mobfilter.RuleCheck.DimensionCheck;
import net.pcal.mobfilter.RuleCheck.EntityIdCheck;
import net.pcal.mobfilter.RuleCheck.LightLevelCheck;
import net.pcal.mobfilter.RuleCheck.MoonPhaseCheck;
import net.pcal.mobfilter.RuleCheck.RandomCheck;
import net.pcal.mobfilter.RuleCheck.SkylightLevelCheck;
import net.pcal.mobfilter.RuleCheck.SpawnReasonCheck;
import net.pcal.mobfilter.RuleCheck.TimeOfDayCheck;
import net.pcal.mobfilter.RuleCheck.WeatherCheck;
import net.pcal.mobfilter.RuleCheck.WorldNameCheck;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

/**
 * Parse mobfilter.json5 and build a config object from it.
 */
@SuppressWarnings("ALL")
public class JsonConfigLoader {

    /**
     * Build the runtime rule structures from the configuration.  Returns null if the configuration contains
     * no rules.
     */
    static void loadRules(final InputStream in, final Config.Builder configBuilder, final Platform platform) throws IOException {
        final JsonConfiguration fromConfig = loadFromJson(in, platform);
        if (fromConfig != null && fromConfig.rules != null) {
            loadRules(fromConfig ,configBuilder, platform);
        }
    }

    static void loadRules(final JsonConfiguration fromConfig, final Config.Builder configBuilder, final Platform platform) {
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
                final EnumSet enumSet = enumSetOf(platform.getSpawnReasonEnum(), when.spawnReason);
                checks.add(new SpawnReasonCheck(enumSet));
            } else if (when.spawnType != null && when.spawnType.length > 0) {
                // legacy support for old name 'spawnType'
                final EnumSet enumSet = enumSetOf(platform.getSpawnReasonEnum(), when.spawnType);
                checks.add(new SpawnReasonCheck(enumSet));
            }
            if (when.category != null && when.category.length > 0) {
                final EnumSet enumSet = enumSetOf(platform.getMobCategoryEnum(), when.category);
                checks.add(new SpawnReasonCheck(enumSet));
            } else if (when.spawnGroup != null && when.spawnGroup.length > 0) {
                // legacy support for old name 'spawnGroup'
                final EnumSet enumSet = enumSetOf(platform.getMobCategoryEnum(), when.spawnGroup);
                checks.add(new CategoryCheck(enumSet));
            }
            if (when.entityId != null) {
                checks.add(new EntityIdCheck(MinecraftIdMatcher.of(when.entityId, platform)));
            }
            if (when.worldName != null) {
                checks.add(new WorldNameCheck(Matcher.of(when.worldName)));
            }
            if (when.dimensionId != null) {
                checks.add(new DimensionCheck(MinecraftIdMatcher.of(when.dimensionId, platform)));
            }
            if (when.biomeId != null) {
                checks.add(new BiomeCheck(MinecraftIdMatcher.of(when.biomeId, platform)));
            }
            if (when.blockId != null) {
                checks.add(new BlockIdCheck(MinecraftIdMatcher.of(when.blockId, platform)));
            }
            if (when.blockX != null) {
                int[] range = parseRange(when.blockX);
                checks.add(new BlockXCheck(range[0], range[1]));
            }
            if (when.blockY != null) {
                int[] range = parseRange(when.blockY);
                checks.add(new BlockYCheck(range[0], range[1]));
            }
            if (when.blockZ != null) {
                int[] range = parseRange(when.blockZ);
                checks.add(new BlockZCheck(range[0], range[1]));
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
            if (when.difficulty != null) {
                checks.add(new DifficultyCheck(Matcher.of(when.difficulty)));
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

    /**
     * So we can create an EnumSet when we don't know the concrete enum type statically.
     */
    private static EnumSet enumSetOf(Class<? extends Enum> enumClass, Enum<?>[] values) {
        final EnumSet enumSet = EnumSet.noneOf(enumClass);
        for (Enum<?> e : values) enumSet.add(e);
        return enumSet;
    }

    static JsonConfiguration loadFromJson(final InputStream in, final Platform patform) throws IOException {
        final String rawJson = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        final Map<String, Class<? extends Enum<?>>> enumFieldTypes = Map.of(
                // Only generic Enum<?> fields need to be mapped here.
                // Concrete enum types (like WeatherType) are detected automatically.
                "spawnReason", patform.getSpawnReasonEnum(),
                "category", patform.getMobCategoryEnum(),
                "difficulty", patform.getDifficultyEnum(),
                "spawnType", patform.getSpawnReasonEnum(),
                "spawnGroup", patform.getMobCategoryEnum()
        );
        final Gson gson = new GsonBuilder().
                setLenient().
                registerTypeAdapterFactory(new EmumAdapterFactory(enumFieldTypes)).
                create();
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
        public Enum<?>[] spawnReason;
        public Enum<?>[] category;
        public String[] blockX;
        public String[] blockY;
        public String[] blockZ;
        public String[] blockId;
        public String[] timeOfDay;
        public String[] lightLevel;
        public String[] skylightLevel;
        public Integer[] moonPhase;
        public WeatherType[] weather;
        public Enum<?>[] difficulty;
        public Double random;

        // for backwards compatibility:
        @Deprecated // use spawnReason instead
        public Enum<?>[] spawnType;
        @Deprecated // use category instead
        public Enum<?>[] spawnGroup;
    }


    // ===================================================================================
    // Private

    /**
     * By default, GSon calls this to silently ignore json keys that don't bind to anything.
     * People then get confused about why their configuration isn't fully working.  This
     * reader fails loudly instead of swallowing the error.
     */
    static class TypoCatchingJsonReader extends JsonReader {
        public TypoCatchingJsonReader(StringReader in) {
            super(in);
            super.setStrictness(Strictness.LENIENT);
        }

        @Override
        public void skipValue()  {
            // This is where it goes when an unknown field is encountered.  Note we don't
            // throw IOException because GSon tries to handle that in a ways that obscures the message.
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

    /**
     * Contends with the fact that some of the bound java fields are of type Enum<?> - we don't know
     * the type at compile time (because it might be in Fabric or Forge).  This adapter handles the
     * deserialization of those types by referencing a provided map of fieldName to concrete enum class.
     *
     * This also provides nicer error messages to the user if they provide an invalid value for a config
     * field of an enum type.
     */
    private static class EmumAdapterFactory implements TypeAdapterFactory {

        private final Map<String, Class<? extends Enum<?>>> fieldEnumTypes;

        EmumAdapterFactory(Map<String, Class<? extends Enum<?>>> fieldEnumTypes) {
            this.fieldEnumTypes = fieldEnumTypes;
        }


        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<?> rawType = type.getRawType();

            // Don't handle arrays - let Gson use its default array adapter
            if (rawType.isArray()) {
                return null;
            }

            // Only handle our config classes that contain enum fields
            if (rawType != JsonConfiguration.class &&
                    rawType != JsonRule.class &&
                    rawType != JsonWhen.class) {
                return null;
            }

            // For objects, delegate to a custom adapter
            return (TypeAdapter<T>) new TypeAdapter<Object>() {
                @Override
                public void write(JsonWriter out, Object value) throws IOException {
                    gson.getAdapter((Class<Object>) value.getClass()).write(out, value);
                }

                @Override
                public Object read(JsonReader in) throws IOException {
                    JsonElement element = JsonParser.parseReader(in);
                    Object instance;
                    try {
                        instance = rawType.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new JsonParseException(e);
                    }
                    for (Field field : rawType.getDeclaredFields()) {
                        try {
                            field.setAccessible(true);
                        } catch (Exception e) {
                            // Skip fields we can't access (e.g., synthetic fields, or in newer Java versions)
                            continue;
                        }
                        JsonElement jsonValue = element.getAsJsonObject().get(field.getName());
                        if (jsonValue == null) continue;

                        Class<?> fieldType = field.getType();
                        String fieldName = field.getName();

                        // First check if it's a concrete enum type (like WeatherType[])
                        Class<? extends Enum<?>> enumClass = getEnumClassFromFieldType(fieldType);

                        // If not a concrete enum, check the map for generic Enum<?> types
                        if (enumClass == null) {
                            enumClass = fieldEnumTypes.get(field.getName());
                        }

                        if (enumClass != null) {
                            if (fieldType.isArray()) {
                                // Handle array of enums
                                JsonArray jsonArray = jsonValue.getAsJsonArray();
                                Enum<?>[] enumArray = (Enum<?>[]) java.lang.reflect.Array.newInstance(enumClass, jsonArray.size());
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    enumArray[i] = validateAndGetEnum(enumClass, jsonArray.get(i).getAsString(), fieldName);
                                }
                                try {
                                    field.set(instance, enumArray);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                // Handle single enum value
                                Enum<?> enumValue = validateAndGetEnum(enumClass, jsonValue.getAsString(), fieldName);
                                try {
                                    field.set(instance, enumValue);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            Object fieldValue = gson.fromJson(jsonValue, field.getType());
                            try {
                                field.set(instance, fieldValue);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    return instance;
                }
            };
        }


        /**
         * Extracts the enum class from a field type. Returns null if the field is not an enum type.
         * Handles both single enum types and arrays of enums.
         */
        @SuppressWarnings("unchecked")
        private Class<? extends Enum<?>> getEnumClassFromFieldType(Class<?> fieldType) {
            if (fieldType.isArray()) {
                // For arrays, get the component type
                Class<?> componentType = fieldType.getComponentType();
                if (Enum.class.isAssignableFrom(componentType) && componentType != Enum.class) {
                    // It's a concrete enum type (not Enum<?>)
                    return (Class<? extends Enum<?>>) componentType;
                }
            } else {
                // For single values, check if it's a concrete enum type
                if (Enum.class.isAssignableFrom(fieldType) && fieldType != Enum.class) {
                    return (Class<? extends Enum<?>>) fieldType;
                }
            }
            return null;
        }

        /**
         * Validates that a string value matches one of the enum constants and returns it.
         * Throws a helpful JsonParseException if the value is invalid.
         */
        @SuppressWarnings("unchecked")
        private Enum<?> validateAndGetEnum(Class<? extends Enum<?>> enumClass, String value, String fieldName) {
            if (Arrays.stream(enumClass.getEnumConstants())
                    .noneMatch(e -> ((Enum<?>) e).name().equals(value))) {
                final StringBuilder msg = new StringBuilder();
                msg.append("Invalid ").append(enumClass.getSimpleName()).append(" value '").append(value).append("'");
                if (fieldName != null) {
                    msg.append(" for field '").append(fieldName).append("'");
                }
                msg.append(".  Valid values are: ");
                boolean isFirst = true;
                for (Enum<?> val : enumClass.getEnumConstants()) {
                    if (isFirst) isFirst = false;
                    else msg.append(", ");
                    msg.append(val.name());
                }
                throw new JsonParseException(msg.toString());
            }
            return Enum.valueOf((Class) enumClass, value);
        }
    }
}
