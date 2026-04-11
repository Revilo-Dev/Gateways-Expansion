package com.revilo.gatewayexpansion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import com.revilo.gatewayexpansion.item.MythicCoinStackData;
import com.revilo.gatewayexpansion.registry.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.UUID;

public final class CoinCommands {

    private static final SimpleCommandExceptionType PLAYER_ONLY = new SimpleCommandExceptionType(Component.translatable("command.gatewayexpansion.coins.player_only"));
    private static final SimpleCommandExceptionType NOT_ENOUGH_COINS = new SimpleCommandExceptionType(Component.translatable("command.gatewayexpansion.coins.not_enough"));
    private static final SimpleCommandExceptionType DIRECT_ID_ONLY = new SimpleCommandExceptionType(Component.translatable("command.gatewayexpansion.coins.direct_id_only"));
    private static final DynamicCommandExceptionType PLAYER_NOT_FOUND = new DynamicCommandExceptionType(id -> Component.translatable("command.gatewayexpansion.coins.player_not_found", id));

    private CoinCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("coins")
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", EntityArgument.player())
                                .executes(context -> resetCoins(context.getSource(), EntityArgument.getPlayer(context, "id")))))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                .then(Commands.argument("id", EntityArgument.player())
                                        .executes(context -> setCoins(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "id"),
                                                IntegerArgumentType.getInteger(context, "count"))))))
                .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                .then(Commands.argument("id", EntityArgument.player())
                                        .executes(context -> addCoins(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "id"),
                                                IntegerArgumentType.getInteger(context, "count"))))))
                .then(Commands.literal("transfer")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> transferCoins(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        -1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> transferCoins(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                IntegerArgumentType.getInteger(context, "count"))))))
                .then(Commands.literal("withdraw")
                        .executes(context -> withdrawCoins(context.getSource(), -1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                .executes(context -> withdrawCoins(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count"))))));

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

    private static int transferCoins(CommandSourceStack source, String targetId, int requestedAmount) throws CommandSyntaxException {
        ServerPlayer sender = source.getPlayerOrException();
        ServerPlayer target = resolveDirectPlayer(source, targetId);
        if (sender == target) {
            return 0;
        }

        int balance = MythicCoinWallet.get(sender);
        int amount = requestedAmount <= 0 ? balance : requestedAmount;
        if (amount <= 0 || balance < amount) {
            throw NOT_ENOUGH_COINS.create();
        }

        MythicCoinWallet.set(sender, balance - amount);
        MythicCoinWallet.addRaw(target, amount);

        source.sendSuccess(() -> Component.translatable("command.gatewayexpansion.coins.transfer.sent", amount, target.getGameProfile().getName()), false);
        target.sendSystemMessage(Component.translatable("command.gatewayexpansion.coins.transfer.received", amount, sender.getGameProfile().getName()));
        return amount;
    }

    private static ServerPlayer resolveDirectPlayer(CommandSourceStack source, String id) throws CommandSyntaxException {
        if (id.startsWith("@")) {
            throw DIRECT_ID_ONLY.create();
        }

        ServerPlayer byName = source.getServer().getPlayerList().getPlayerByName(id);
        if (byName != null) {
            return byName;
        }

        try {
            UUID uuid = UUID.fromString(id);
            ServerPlayer byUuid = source.getServer().getPlayerList().getPlayer(uuid);
            if (byUuid != null) {
                return byUuid;
            }
        } catch (IllegalArgumentException ignored) {
        }

        throw PLAYER_NOT_FOUND.create(id);
    }

    private static int withdrawCoins(CommandSourceStack source, int requestedAmount) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int balance = MythicCoinWallet.get(player);
        int amount = requestedAmount <= 0 ? balance : requestedAmount;
        if (amount <= 0 || balance < amount) {
            throw NOT_ENOUGH_COINS.create();
        }

        MythicCoinWallet.set(player, balance - amount);
        giveCoinsAsItems(player, amount);
        source.sendSuccess(() -> Component.translatable("command.gatewayexpansion.coins.withdraw", amount), false);
        return amount;
    }

    private static void giveCoinsAsItems(ServerPlayer player, int amount) {
        ItemStack stack = MythicCoinStackData.createStack(amount);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
