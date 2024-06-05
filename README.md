# MobFilter

MobFilter is a Minecraft mod that allows you to limit spawning of mobs in your world.  
You can use it to
* Create safe zones in your world where mobs aren't allowed to spawn.
* Completely prevent particular mobs from ever spawning
* Limit mob spawning to specific biomes, times, or light levels
* ...and more

MobFilter uses the Fabric modloader and runs only on the server.

I will not be doing a Forge version of MobFilter.

## Usage

MobFilter uses a flexible, rule-based filtering system that you configure.  When Minecraft wants to spawn
a new mob, MobFilter checks the rules you provide to see if the spawn should be 'vetoed.'

The first time you run Minecraft with the mobfilter jar installed, an empty configuration file will be
created in `config/mobfilter.yml`.  Just edit this file to set up your rules.  The rules will take effect the
next time you start a world.

Rules can test for several conditions, including:
* Block Position
* Location (Biome, World, Dimension, BlockId)
* Mob Type (EntiyId or SpawnGroup)
* Time of Day
* Light Level
* Phase of moon

See [CONFIG.md](CONFIG.md) for more information aboutsetting up rules.


## Legal

This mod is published under the [Apache 2.0 License](LICENSE).

You're free to include this mod in your modpack provided you attribute it to pcal.net.

## Questions?

If you have questions about this mod, please join the Discord server:

[https://discord.pcal.net](https://discord.pcal.net)

Comments have been disabled and I will **not** reply to private messages on Curseforge.
