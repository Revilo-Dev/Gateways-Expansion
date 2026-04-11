package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.GatewayExpansion;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;

public class GatewaySwordItem extends SwordItem implements RarityTintedItemName {

    private static final ResourceLocation CRIT_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "sword_crit_damage");
    private final ChatFormatting nameColor;
    private final int slownessDuration;
    private final int poisonDuration;
    private final int fireSeconds;
    private final int runeSlots;
    private final List<Component> implicitTooltip;

    public GatewaySwordItem(
            Tier tier,
            Properties properties,
            ChatFormatting nameColor,
            float attackDamage,
            float attackSpeed,
            int slownessDuration,
            int poisonDuration,
            int fireSeconds,
            double critDamageBonus,
            int runeSlots,
            List<Component> implicitTooltip) {
        super(tier, properties.attributes(withCritDamage(tier, attackDamage, attackSpeed, critDamageBonus)));
        this.nameColor = nameColor;
        this.slownessDuration = slownessDuration;
        this.poisonDuration = poisonDuration;
        this.fireSeconds = fireSeconds;
        this.runeSlots = runeSlots;
        this.implicitTooltip = List.copyOf(implicitTooltip);
    }

    @Override
    public ChatFormatting nameColor() {
        return this.nameColor;
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.tintedName(stack, super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        for (Component line : this.implicitTooltip) {
            tooltipComponents.add(line.copy().withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (this.slownessDuration > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, this.slownessDuration, 0), attacker);
        }
        if (this.poisonDuration > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, this.poisonDuration, 0), attacker);
        }
        if (this.fireSeconds > 0 && !target.fireImmune()) {
            target.igniteForSeconds(this.fireSeconds);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        RunicItemSupport.ensureRunicData(stack, this.runeSlots);
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private static ItemAttributeModifiers withCritDamage(Tier tier, float attackDamage, float attackSpeed, double critDamageBonus) {
        ItemAttributeModifiers modifiers = SwordItem.createAttributes(tier, attackDamage, attackSpeed);
        if (critDamageBonus <= 0.0D) {
            return modifiers;
        }
        return modifiers.withModifierAdded(
                ALObjects.Attributes.CRIT_DAMAGE,
                new AttributeModifier(CRIT_DAMAGE_ID, critDamageBonus, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }
}
