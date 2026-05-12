package com.revilo.gatesofavarice.integration;

import java.lang.reflect.Method;
import net.minecraft.client.gui.GuiGraphics;

public final class LevelUpClientIntegration {

    private static Method getLevelBarWidthMethod;
    private static Method renderPlayerLevelBarMethod;
    private static boolean initialized;

    private LevelUpClientIntegration() {
    }

    public static int getLevelBarWidth() {
        init();
        if (getLevelBarWidthMethod == null) {
            return 182;
        }
        try {
            return (int) getLevelBarWidthMethod.invoke(null);
        } catch (ReflectiveOperationException ignored) {
            return 182;
        }
    }

    public static boolean renderPlayerLevelBar(GuiGraphics guiGraphics, int x, int y) {
        init();
        if (renderPlayerLevelBarMethod == null) {
            return false;
        }
        try {
            renderPlayerLevelBarMethod.invoke(null, guiGraphics, x, y);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        for (String className : new String[] {
                "com.revilo.levelup.api.client.LevelUpClientApi",
                "com.revilo.levelup.api.LevelUpClientApi",
                "LevelUpClientApi"
        }) {
            try {
                Class<?> api = Class.forName(className);
                getLevelBarWidthMethod = api.getMethod("getLevelBarWidth");
                renderPlayerLevelBarMethod = api.getMethod("renderPlayerLevelBar", GuiGraphics.class, int.class, int.class);
                return;
            } catch (ReflectiveOperationException ignored) {
                getLevelBarWidthMethod = null;
                renderPlayerLevelBarMethod = null;
            }
        }
    }
}
