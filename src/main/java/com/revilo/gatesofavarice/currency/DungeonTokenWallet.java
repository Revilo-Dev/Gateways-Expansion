package com.revilo.gatesofavarice.currency;

import com.revilo.gatesofavarice.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSync;

public final class DungeonTokenWallet {

    private DungeonTokenWallet() {
    }

    public static int get(Player player) {
        return player.getData(ModAttachments.DUNGEON_TOKENS);
    }

    public static void add(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }
        set(player, get(player) + amount);
    }

    public static void set(ServerPlayer player, int amount) {
        player.setData(ModAttachments.DUNGEON_TOKENS, Math.max(0, amount));
        AttachmentSync.syncEntityUpdate(player, ModAttachments.DUNGEON_TOKENS.get());
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
