package com.revilo.gatewayexpansion.registry;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.block.GatewayWorkbenchBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GatewayExpansion.MOD_ID);

    public static final DeferredBlock<Block> GATEWAY_WORKBENCH = BLOCKS.register("gateway_workbench", () -> new GatewayWorkbenchBlock());

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
