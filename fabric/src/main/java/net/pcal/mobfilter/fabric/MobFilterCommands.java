package net.pcal.mobfilter.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.pcal.mobfilter.ConfigService;

public class MobFilterCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("mobfilter")
                        .requires(source -> source.hasPermission(2)) // OP level 2
                        .then(Commands.literal("on")
                                .executes(ctx -> {
                                    ConfigService.get().setEnabled(true);
                                    ctx.getSource().sendSuccess(() -> Component.literal("MobFilter enabled"), true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("off")
                                .executes(ctx -> {
                                    ConfigService.get().setEnabled(false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("MobFilter disabled"), true);
                                    return 1;
                                })
                        )
        );
    }
}