package com.revilo.gatewayexpansion.client;

import com.revilo.gatewayexpansion.integration.LevelUpIntegration;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import java.lang.reflect.Method;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class LevelUpGateOverlay {

    private static final String LEVELUP_OVERLAY_CLASS = "com.revilo.levelup.client.hud.TopCenterLevelOverlay";
    private static Method setStayOnScreenLockMethod;
    private static boolean stayOnScreenLookupAttempted;
    private static Boolean currentStayOnScreenLock;

    private LevelUpGateOverlay() {
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null || minecraft.options.hideGui || !LevelUpIntegration.isLoaded()) {
            applyStayOnScreenLock(null);
            return;
        }

        GatewayEntity gateway = findNearbyGateway(player);
        boolean gateActive = gateway != null;
        applyStayOnScreenLock(gateActive ? Boolean.TRUE : null);
    }

    private static GatewayEntity findNearbyGateway(LocalPlayer player) {
        GatewayEntity nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (GatewayEntity candidate : player.level().getEntitiesOfClass(GatewayEntity.class, player.getBoundingBox().inflate(96.0D))) {
            if (!candidate.isValid() || ShopkeeperManager.isGatewayAnimation(candidate)) {
                continue;
            }

            double distance = player.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static boolean isStayOnScreenHookAvailable() {
        initStayOnScreenHook();
        return setStayOnScreenLockMethod != null;
    }

    private static void applyStayOnScreenLock(Boolean lockState) {
        if (!isStayOnScreenHookAvailable()) {
            return;
        }
        if (Objects.equals(currentStayOnScreenLock, lockState)) {
            return;
        }
        currentStayOnScreenLock = lockState;
        try {
            setStayOnScreenLockMethod.invoke(null, lockState);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void initStayOnScreenHook() {
        if (stayOnScreenLookupAttempted) {
            return;
        }
        stayOnScreenLookupAttempted = true;
        try {
            Class<?> overlayClass = Class.forName(LEVELUP_OVERLAY_CLASS);
            setStayOnScreenLockMethod = resolveStayOnScreenMethod(overlayClass);
        } catch (ReflectiveOperationException ignored) {
            setStayOnScreenLockMethod = null;
        }
    }

    private static Method resolveStayOnScreenMethod(Class<?> overlayClass) {
        Method method = findMethod(overlayClass, "setEventStayOnScreenLock", Boolean.class);
        return method != null ? method : findMethod(overlayClass, "setEventStayOnScreenLock", boolean.class);
    }

    private static Method findMethod(Class<?> targetClass, String name, Class<?> parameterType) {
        try {
            return targetClass.getMethod(name, parameterType);
        } catch (NoSuchMethodException ignored) {
            try {
                Method declared = targetClass.getDeclaredMethod(name, parameterType);
                declared.setAccessible(true);
                return declared;
            } catch (ReflectiveOperationException ignoredAgain) {
                return null;
            }
        }
    }
}
