package com.revilo.gatewayexpansion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import com.revilo.gatewayexpansion.registry.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CoinCommands {

    private static final SimpleCommandExceptionType PLAYER_ONLY = new SimpleCommandExceptionType(Component.translatable("command.gatewayexpansion.coins.player_only"));
    private static final SimpleCommandExceptionType NOT_ENOUGH_COINS = new SimpleCommandExceptionType(Component.translatable("command.gatewayexpansion.coins.not_enough"));

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
                        .then(Commands.argument("id", EntityArgument.player())
                                .executes(context -> transferCoins(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "id"),
                                        -1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> transferCoins(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "id"),
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

    private static int transferCoins(CommandSourceStack source, ServerPlayer target, int requestedAmount) throws CommandSyntaxException {
        ServerPlayer sender = source.getPlayerOrException();
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
        int remaining = amount;
        int maxStackSize = ModItems.MYTHIC_COIN.get().getDefaultMaxStackSize();
        while (remaining > 0) {
            int stackSize = Math.min(maxStackSize, remaining);
            ItemStack stack = new ItemStack(ModItems.MYTHIC_COIN.get(), stackSize);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            remaining -= stackSize;
        }
    }
}
