package com.revilo.gatewayexpansion.item;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class PaxelItem extends TieredItem {

    public PaxelItem(Tier tier, Item.Properties properties, float attackDamage, float attackSpeed) {
        super(
                tier,
                properties
                        .attributes(DiggerItem.createAttributes(tier, attackDamage, attackSpeed))
                        .component(
                                DataComponents.TOOL,
                                new Tool(
                                        ImmutableList.of(
                                                Tool.Rule.deniesDrops(tier.getIncorrectBlocksForDrops()),
                                                Tool.Rule.minesAndDrops(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, tier.getSpeed()),
                                                Tool.Rule.minesAndDrops(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE, tier.getSpeed()),
                                                Tool.Rule.minesAndDrops(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL, tier.getSpeed()),
                                                Tool.Rule.minesAndDrops(net.minecraft.tags.BlockTags.MINEABLE_WITH_HOE, tier.getSpeed())),
                                        1.0F,
                                        1)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (playerHasShieldUseIntent(context)) {
            return InteractionResult.PASS;
        }

        Optional<BlockState> stripped = tryAxeAction(context);
        if (stripped.isPresent()) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        BlockState originalState = context.getLevel().getBlockState(context.getClickedPos());
        BlockState flattenState = originalState.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
        if (flattenState != null && HoeItem.onlyIfAirAbove(context)) {
            return applyStateChange(context, flattenState);
        }

        BlockState pathState = ShovelItem.getShovelPathingState(originalState);
        if (pathState != null && HoeItem.onlyIfAirAbove(context)) {
            return applyStateChange(context, pathState);
        }

        BlockState tillState = originalState.getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
        if (tillState != null) {
            return applyStateChange(context, tillState);
        }

        return InteractionResult.PASS;
    }

    private Optional<BlockState> tryAxeAction(UseOnContext context) {
        BlockState originalState = context.getLevel().getBlockState(context.getClickedPos());
        BlockState modified = originalState.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);
        if (modified == null) {
            modified = originalState.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false);
        }
        if (modified == null) {
            modified = originalState.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false);
        }
        if (modified == null) {
            modified = AxeItem.getAxeStrippingState(originalState);
        }
        if (modified == null) {
            return Optional.empty();
        }

        applyStateChange(context, modified);
        return Optional.of(modified);
    }

    private InteractionResult applyStateChange(UseOnContext context, BlockState state) {
        if (!context.getLevel().isClientSide) {
            context.getLevel().setBlock(context.getClickedPos(), state, 11);
            context.getLevel().gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, context.getClickedPos(), net.minecraft.world.level.gameevent.GameEvent.Context.of(context.getPlayer(), state));
            if (context.getPlayer() != null) {
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    private static boolean playerHasShieldUseIntent(UseOnContext context) {
        Player player = context.getPlayer();
        return player != null
                && context.getHand() == InteractionHand.MAIN_HAND
                && player.getOffhandItem().is(net.minecraft.world.item.Items.SHIELD)
                && !player.isSecondaryUseActive();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
    }
}
