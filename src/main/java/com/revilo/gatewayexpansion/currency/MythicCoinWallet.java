package com.revilo.gatewayexpansion.currency;

import com.revilo.gatewayexpansion.registry.ModAttachments;
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
        if (amount <= 0) {
            return;
        }
        player.setData(ModAttachments.MYTHIC_COINS, get(player) + amount);
        AttachmentSync.syncEntityUpdate(player, ModAttachments.MYTHIC_COINS.get());
    }

    public static boolean spend(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return true;
        }
        int current = get(player);
        if (current < amount) {
            return false;
        }
        player.setData(ModAttachments.MYTHIC_COINS, current - amount);
        AttachmentSync.syncEntityUpdate(player, ModAttachments.MYTHIC_COINS.get());
        return true;
    }
}
