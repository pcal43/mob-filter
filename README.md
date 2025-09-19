# MobFilter

MobFilter is a Minecraft mod that allows you to limit spawning of mobs in 
your world.  You can use it to
* Completely prevent particular mobs from ever spawning
* Create safe zones in your world where mobs aren't allowed to spawn.
* Limit mob spawning to specific biomes, times, or light levels
* ...and more

MobFilter uses the Fabric modloader and runs only on the server.

## Usage

MobFilter uses a flexible, rule-based filtering system that you configure. 
When Minecraft wants to spawn a new mob, MobFilter checks the rules you 
provide to see if the spawn should be 'vetoed.'

The first time you run Minecraft with MobFilter installed, two empty 
configuration files will be created in the `config` directory of your 
minecraft installation: `mobfilter.simple` and `mobfilter.json5`.  


### Simple Configuration (`mobfilter.simple`)

If you just want to remove some mobs from the game, edit
[`mobfilter.simple`](docs/simple-configuration.md) and add a list of the
mob ids that you want to prevent spawning:

```
# I never want to see these mobs again:
minecraft:creeper
minecraft:phantom
minecraft:silverfish
```

See the [documentation](docs/simple-configuration.md) for more details.


### Advanced Configuration (`mobfilter.json5`)

If you need more detailed control over mob spawning, you can instead edit
[`mobfilter.json5`](docs/advanced-configuration.md).  This file is a bit more
complicated to configure but it allows your filtering logic to test for more
complicated conditions, including:
* Block Position
* Location (Biome, World, Dimension, BlockId)
* Mob Type (EntiyId or SpawnGroup)
* Time of Day
* Light Level
* Phase of moon

For example, if you want to prevent hostile mobs from spawning in a specific
area, you could add a rule like this:

```
{ 
  rules : [
    {
      what : 'DISALLOW_SPAWN',
      when : {
        category : [ 'MONSTER' ],
        dimensionId : [ 'minecraft:overworld' ],
        blockX: [ -128, 234 ],
        blockY : [ 63, 'MAX' ],
        blockZ : [ -321, 512 ]
      }
    }
  ]
}
```

See the [documentation](docs/advanced-configuration.md) for more details.

## Why This Mod?

I wrote this mod because I wanted to be able to be able to play survival 
minecraft with my daughter, and that meant we had to have a mob-free area 
around our base. There are numerous other mods out there that manage mob 
spawning but none of them did quite what I needed them to do.

## Backports?

As a rule, I don't do backports unless critical security issues are involved. 
It's just too much tedious work to backport every mod feature to every old 
version.  But I'm happy to accept PRs if you want to do one yourself.

## Legal

This mod is published under the [MIT License](LICENSE).

You're free to include this mod in your modpack provided you attribute it 
to pcal.net.

## Questions?

If you have questions about this mod, please join the Discord server:

[https://discord.pcal.net](https://discord.pcal.net)
