package com.revilo.gatewayexpansion.currency;

import com.revilo.gatewayexpansion.registry.ModAttachments;
import com.revilo.gatewayexpansion.registry.ModAttributes;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSync;

public final class MythicCoinWallet {

    private MythicCoinWallet() {
    }

    public static int get(Player player) {
        return player.getData(ModAttachments.MYTHIC_COINS);
    }

    public static void add(ServerPlayer player, int amount) {
        addScaled(player, amount);
    }

    public static void addRaw(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }
        player.setData(ModAttachments.MYTHIC_COINS, get(player) + amount);
        AttachmentSync.syncEntityUpdate(player, ModAttachments.MYTHIC_COINS.get());
    }

    public static void set(ServerPlayer player, int amount) {
        player.setData(ModAttachments.MYTHIC_COINS, Math.max(0, amount));
        AttachmentSync.syncEntityUpdate(player, ModAttachments.MYTHIC_COINS.get());
    }

    public static float getCommandMultiplier(Player player) {
        return player.getData(ModAttachments.COIN_MULTIPLIER);
    }

    public static void setCommandMultiplier(ServerPlayer player, float multiplier) {
        player.setData(ModAttachments.COIN_MULTIPLIER, Math.max(0.0F, multiplier));
        AttachmentSync.syncEntityUpdate(player, ModAttachments.COIN_MULTIPLIER.get());
    }

    public static double getTotalMultiplier(Player player) {
        double attributeMultiplier = player.getAttributeValue(ModAttributes.COIN_MULTIPLIER);
        return Math.max(0.0D, getCommandMultiplier(player)) * Math.max(0.0D, attributeMultiplier);
    }

    private static void addScaled(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }

        int adjustedAmount = Mth.floor((float) (amount * getTotalMultiplier(player)) + 0.5F);
        if (adjustedAmount <= 0) {
            return;
        }

        addRaw(player, adjustedAmount);
    }

    public static boolean spend(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return true;
        }
        int current = get(player);
        if (current < amount) {
            return false;
        }
        set(player, current - amount);
        return true;
    }
}
