package com.revilo.gatesofavarice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.revilo.gatesofavarice.dungeon.DungeonRunManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class DungeonCommands {

    private DungeonCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dungeon")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("recover")
                        .executes(context -> recover(context.getSource()))));
    }

    private static int recover(CommandSourceStack source) {
        int recovered = DungeonRunManager.recoverStalledShops(source.getServer());
        source.sendSuccess(() -> Component.literal("Recovered dungeon runs: " + recovered), true);
        return recovered;
    }
}
