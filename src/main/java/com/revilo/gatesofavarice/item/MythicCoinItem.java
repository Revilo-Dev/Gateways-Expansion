package com.revilo.gatesofavarice.item;

import com.revilo.gatesofavarice.currency.MythicCoinWallet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

public class MythicCoinItem extends Item {

    public MythicCoinItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.mythic_coin.value", MythicCoinStackData.getValue(stack)).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.mythic_coin.redeem").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        int value = MythicCoinStackData.getValue(stack);
        if (value <= 0) {
            return InteractionResultHolder.pass(stack);
        }

        MythicCoinWallet.add(serverPlayer, value);
        stack.shrink(stack.getCount());
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
