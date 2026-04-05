package com.revilo.gatewayexpansion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CoinCommands {

    private static final SimpleCommandExceptionType PLAYER_ONLY = new SimpleCommandExceptionType(Component.translatable("command.gatewayexpansion.coins.player_only"));

    private CoinCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("coins")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .then(Commands.argument("id", EntityArgument.player())
                                .executes(context -> resetCoins(context.getSource(), EntityArgument.getPlayer(context, "id")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                .then(Commands.argument("id", EntityArgument.player())
                                        .executes(context -> setCoins(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "id"),
                                                IntegerArgumentType.getInteger(context, "count"))))))
                .then(Commands.literal("add")
                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                .then(Commands.argument("id", EntityArgument.player())
                                        .executes(context -> addCoins(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "id"),
                                                IntegerArgumentType.getInteger(context, "count")))))));

        dispatcher.register(Commands.literal("coin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("multiplier")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F))
                                .executes(context -> setMultiplier(
                                        context.getSource(),
                                        FloatArgumentType.getFloat(context, "value"))))));
    }

    private static int resetCoins(CommandSourceStack source, ServerPlayer target) {
        MythicCoinWallet.set(target, 0);
        source.sendSuccess(() -> Component.translatable("command.gatewayexpansion.coins.reset", target.getGameProfile().getName()), true);
        return 1;
    }

    private static int setCoins(CommandSourceStack source, ServerPlayer target, int amount) {
        MythicCoinWallet.set(target, amount);
        source.sendSuccess(() -> Component.translatable("command.gatewayexpansion.coins.set", amount, target.getGameProfile().getName()), true);
        return amount;
    }

    private static int addCoins(CommandSourceStack source, ServerPlayer target, int amount) {
        MythicCoinWallet.addRaw(target, amount);
        source.sendSuccess(() -> Component.translatable("command.gatewayexpansion.coins.add", amount, target.getGameProfile().getName()), true);
        return amount;
    }

    private static int setMultiplier(CommandSourceStack source, float multiplier) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MythicCoinWallet.setCommandMultiplier(player, multiplier);
        source.sendSuccess(() -> Component.translatable("command.gatewayexpansion.coins.multiplier", multiplier), true);
        return 1;
    }
}
