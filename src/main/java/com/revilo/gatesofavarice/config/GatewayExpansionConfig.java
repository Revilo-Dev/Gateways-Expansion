package com.revilo.gatesofavarice.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GatewayExpansionConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ANNOUNCE_GATE_OPEN_IN_CHAT;
    public static final ModConfigSpec.BooleanValue ANNOUNCE_GATE_PARTY_JOIN_IN_CHAT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("gateway");
        ANNOUNCE_GATE_OPEN_IN_CHAT = builder.comment("Broadcast a chat message when a player opens a generated gate.")
                .define("announceGateOpenInChat", true);
        ANNOUNCE_GATE_PARTY_JOIN_IN_CHAT = builder.comment("Broadcast a chat message when another player joins an active generated gate.")
                .define("announceGatePartyJoinInChat", true);
        builder.pop();
        SPEC = builder.build();
    }

    private GatewayExpansionConfig() {
    }
}
