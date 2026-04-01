package com.revilo.gatewayexpansion.menu;

import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import com.revilo.gatewayexpansion.registry.ModMenus;
import com.revilo.gatewayexpansion.shop.ShopOfferDefinition;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ShopkeeperMenu extends AbstractContainerMenu {

    private final Player player;
    private final int shopkeeperId;

    public ShopkeeperMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, extraData.readInt());
    }

    public ShopkeeperMenu(int containerId, Inventory inventory, int shopkeeperId) {
        super(ModMenus.SHOPKEEPER.get(), containerId);
        this.player = inventory.player;
        this.shopkeeperId = shopkeeperId;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        Entity entity = serverPlayer.level().getEntity(this.shopkeeperId);
        if (!(entity instanceof WanderingTrader trader) || !ShopkeeperManager.isShopkeeper(trader)) {
            return false;
        }

        java.util.List<ShopOfferDefinition> offers = ShopkeeperManager.getOffers(trader);
        if (id < 0 || id >= offers.size()) {
            return false;
        }

        ShopOfferDefinition offer = offers.get(id);
        if (!MythicCoinWallet.spend(serverPlayer, offer.price())) {
            serverPlayer.displayClientMessage(Component.translatable("message.gatewayexpansion.not_enough_mythic_coins", offer.price()), true);
            return false;
        }

        ItemStack reward = offer.createStack(serverPlayer.getRandom());
        if (!serverPlayer.getInventory().add(reward)) {
            serverPlayer.drop(reward, false);
        }
        serverPlayer.sendSystemMessage(Component.translatable("message.gatewayexpansion.purchase_complete", offer.title(), offer.price()));
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        Entity entity = player.level().getEntity(this.shopkeeperId);
        return entity instanceof WanderingTrader trader
                && trader.isAlive()
                && ShopkeeperManager.isShopkeeper(trader)
                && player.distanceToSqr(trader) <= 64.0D;
    }

    public int getWalletBalance() {
        return MythicCoinWallet.get(this.player);
    }

    public java.util.List<ShopOfferDefinition> getOffers() {
        Entity entity = this.player.level().getEntity(this.shopkeeperId);
        if (entity instanceof WanderingTrader trader && ShopkeeperManager.isShopkeeper(trader)) {
            return ShopkeeperManager.getOffers(trader);
        }
        return ShopOfferDefinition.CORE_OFFERS;
    }
}
