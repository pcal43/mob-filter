# Configuring Mob-Filter

To use mob filter, you create a list of rules that can 'veto' the spawning of a mobs in the game
based on specific conditions.  Rules are configured in `mobfilter.json5` in your minecraft `config` 
folder.

*To see example configurations that you can copy-paste, skip ahead to the [Examples](#examples) section.*

## Declaring Rules

The top-level 'rules' configuration element contains a list of filtering rules that are checked to see whether
a new mob should be allowed to spawn.

Each rule has three keys:
- **`name`** - A human-readable name for the rule, useful for documentation and debugging.
- **`what`** - Indicates what to do when the rule matches.  Must be DISALLOW_SPAWN or ALLOW_SPAWN.
- **`when`** - A list of conditions that must be true in order for the rule to match.

The `when` section provides a set of conditions that must all be true in order for the rule to be a match.
It may contain any of the following keys:



### 'When' Keys

Each `rule` has a `when` map that contains describes specific tests that are evaluated as part of the rules.
Valid keys for the `when` map are described below.


#### `entityId`
A list of entity ids, e.g. 'minecraft:zombie'.  The condition is true when the id of the mob to be spawned is
in the list.

#### `category`

List of net.minecraft.entity.MobCategory values.  The condition is true when# the mob to be spawned is in one of
the listed categories.  

*In version 0.8.0+1.20.4 and older, this key was called `spawnType`.*

As of Minecraft 1.20.4, valid values are:
- `MONSTER`
- `CREATURE`
- `AMBIENT`
- `AXOLOTLS`
- `UNDERGROUND_WATER_CREATURE`
- `WATER_CREATURE`
- `WATER_AMBIENT`
- `MISC`

#### `blockX`, `blockY` and `blockZ`
Each of these is a list of two integers specifying a range.  True when the mob's spawning position on the given axis 
(X, Y or Z) is within the provided range.  `MIN` may be used for the first value and `MAX` may be used for the second.

#### `worldName`
List of world names, e.g., 'My World'.  True if mob is spawning in a listed world.  Useful if you need
to limit mob filtering to specific worlds.

#### `dimensionId`
List of dimension identifiers, e.g., 'minecraft:overworld'.  True if mob is spawning in a listed dimension.
You can match all dimensions in a given namespace using `*`; for example `minecraft:*` will match all vanilla dimensions.

#### `biomeId`
List of biome identifiers, e.g., 'minecraft:ocean'.  True if mob is spawning in a listed biome.
You can match all biomes in a given namespace using `*`; for example `minecraft:*` will match all vanilla biomes.

#### `blockId`
List of block identifiers, e.g., 'minecraft:cobblestone'.  True if mob is spawning on a listed block type.
You can match all blocks in a given namespace using `*`; for example `minecraft:*` will match all vanilla blocks.

#### `lightLevel`
Two integers between 0 and 16.  True if mob is spawning in a lightLevel within the range.

#### `skylightLevel`
*Only available in versions `0.14.3+1.21.4` and later.*  Two integers 0 and 15, inclusive.  True if a mob is spawning on a block whose 
[Sky Light level](https://minecraft.wiki/w/Light#Sky_light) is within the range.

#### `moonPhase`
List of integers between 1 (Full Moon) and 8, inclusive, [indicating a phase of the moon](https://minecraft.wiki/w/Moon#Phases).
True if mob is spawning when the moon is at a phase number that is in the list.

#### `timeOfDay`
Two integers between 0 and 24000.  True if mob is spawning at a timeOfDay within the range.

#### `weather`
*Only available in version `0.16.1+1.21.6` and greater.*
List of weather types: CLEAR, RAIN, SNOW, THUNDER.
True if the mob is spawning on a block that has the given weather type.

#### `random`
*Only available in version `0.16.1+1.21.6` and greater.*
A value between 0 and 1.  
True if a freshly-generated random number is less than this value.

#### `spawnReason`
*In version `0.11.2+1.21.1` and older, this key was called `spawnType`.* A list of net.minecraft.entity.EntitySpawnReason values.  The condition is true when the mob is being spawned due
to one of the listed reasons.

As of Minecraft 1.21.3, valid values are:
- `NATURAL`
- `CHUNK_GENERATION`
- `SPAWNER`
- `STRUCTURE`
- `BREEDING`
- `MOB_SUMMONED`
- `JOCKEY`
- `EVENT`
- `CONVERSION`
- `REINFORCEMENT`
- `TRIGGERED`
- `BUCKET`
- `SPAWN_ITEM_USE`
- `COMMAND`
- `DISPENSER`
- `PATROL`
- `TRIAL_SPAWNER`
- `LOAD`
- `DIMENSION_TRAVEL`

Note that mob-filter may be unable to filter spawns in some of these cases.


## Order Matters

Filter rules are processed in order and processing stops as soon as a matching rule is found.  This means you can
put blanket exclusions at the top of your rules list like this:
```
{
    "rules" : [
        {
            "name" : "If we're in the nether, allow the spawn and ignore later rules.",
            "what" : "ALLOW_SPAWN",
            "when" : {
                "dimensionId" : ["minecraft:the_nether"]
            }
        },
        {
            "name" : "...but everywhere else, disallow all spawns above sea level",
            "what" : "DISALLOW_SPAWN",
            "when" : {
                "blockY" : ["MIN", 62]
            }
        }
    ]
}
```

## Debugging

After the `rules` section, you can specify `logLevel` to set the log4j logging level for the mod.  Set to 'DEBUG' 
to log each time a spawn is disallowed.  Set to 'TRACE' to see detailed logging on rule evaluation.  

Be careful - this can quickly fill up your logfiles.


## mobfilter.yaml

Older versions of mob-filter were configured via a yaml file.  This is deprecated - please use the json5 format
instead.  Suggested steps:
- Convert your yaml file to json using an [online conversion tool](https://onlineyamltools.com/convert-yaml-to-json).
- Paste the converted file into `mobfilter.json5` in your `config` folder.
- Rename `mobfilter.yaml` to `mobfilter.yaml.ignored` so that it will be ignored by the mod.
- When you're satisfied everything is working correctly, you can deleted `mobfilter.yaml.ignored`

## Caveats

- You can't use mob-filter to spawn additional mobs, fewer mobs, or to change which mobs are spawned.  It just lets you
prevent specific mobs from spawning.
- mob-filter may be unable to block spawning of some special kinds of mobs.  Check the Issues tab for details.


## Examples

### Disable Creepers
```
{
    "rules" : [
        {
            "what" : "DISALLOW_SPAWN",
            "when" : {
                "entityId" : [ "minecraft:creeper" ]
            }
        }
    ]
}
```

### Disable All Vanilla Mobs
```
{
    "rules" : [
        {
            "name" : "No Vanilla",
            "what" : "DISALLOW_SPAWN",
            "when" : {
                "entityId" : [ "minecraft:*" ]
            }
        }
    ]
}
```

### Secret Cow Level

```
{
    "rules" : [
        {
            "name" : "Allow cows...",
            "what" : "ALLOW_SPAWN",
            "when" : {
                "entityId" : [ "minecraft:cow" ]
            }
         },
        {
            "name" : "...and nothing else",
            "what" : "DISALLOW_SPAWN",
            "when" : {}
        }
    ]
}
```

### No Freshwater Squid
```
{
    "rules" : [
        {
            "what" : "DISALLOW_SPAWN",
            "when" : {
                "entityId" : [ "minecraft:squid" ],
                "biomeId" : [ "minecraft:river", "minecraft:frozen_river" ]
            }
       }
    ]
}
```

### Safe Zone
```
{ 
    "rules" : [
        {
            "what" : "DISALLOW_SPAWN",
            "when" : {
                "category" : [ "MONSTER" ],
                "dimensionId" : [ "minecraft:overworld" ],
                "blockX" : [ -128, 234 ],
                "blockY" : [ 63, "MAX" ],
                "blockZ" : [ -321, 512 ]
            }
        }
    ]
}
```

### Full Moon Zombie Party
```
{ 
    "rules" : [
        {
            "what" : "ALLOW_SPAWN",
            "when" : {
                "entityId" : [ "minecraft:zombie" ],
                "moonPhase" : [ 1 ]
            }
        },
        {
            "name" : "disallow non-zombie monsters during full moon",
            "what" : "DISALLOW_SPAWN",
            "when" : {
                "category" : [ "MONSTER" ],
                "moonPhase" : [ 1 ]
            }
        }
    ]
}
```
