# Simple Configuration (`mobfilter.simple`)

The simple configuration file is located here:

```
[minecraft directory]/config/mobfilter.simple
```

It allows you to eliminate specific mods from your game by simply listing out 
their entity IDs, one per line.  For example:

```
# (lines beginning with a '#' are comments and will be ignored)
# I hate these mobs and want them gone forever:

minecraft:creeper
minecraft:phantom
minecraft:silverfish
```

If the file doesn't exist, an empty placeholder file will be generated the 
first time you start minecraft with mob-filter installed.  You can then edit
the file and restart your world for the changes to take effect.

## Wildcards

Note that in the lines above, the entity ids must include the `minecraft:`
namespace. This could instead be a namespace from another mod (such as
`cobblemon`) that adds mobs.

If you want to filter out _all_ of the mobs in a namespace, you can use a 
`*` wildcard character rather than listing them all out:

```
# A dead world with NO vanilla mobs:

minecraft:*
```

*IMPORTANT: you can't use the wildcard character anywhere you want - doing
something like `minecraft:pig*` will result in an error.  The wild card has
to come after the `:` separator and has to be the last thing in the pattern.*


## Allowing Just Some Mobs

You can also use a `!` character at the start of a line to indicate that 
you _do_ want the specific mob to spawn. When combined with Namespace 
Wildcards, this allows you to easily keep just a few specific mobs around:

```
# I like these animals and want them in my world.  
# Using the ! means "don't filter these out"
!minecraft:cow
!minecraft:sheep
!minecraft:chicken
!minecraft:cat

# But I don't want any other mobs in my world:
minecraft:*
```

## Order Matters

An important thing to understand in the example above is that _order matters_
in the list.  When a mob is going to spawn, Mob Filter goes down the list 
trying to find a matching line.  As soon as it finds one, it stops looking 
and either stops the spawn or (if the line starts with `!`) allows it.

If Mob Filter gets to the end of the list without finding a match, the spawn
is allowed.

## Advanced Configuration

If you want more control over which mobs are filtered, you can use the full 
json5 configuration file (`mobfilter.json5`) instead.  This will allow you to
filter mobs based on location, dimension, weather, time of day, and much more.

See the [Advanced Configuration](advanced-configuration.md) page for details.
