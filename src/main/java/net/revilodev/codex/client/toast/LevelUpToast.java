package net.revilodev.codex.client.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.revilodev.codex.skills.SkillCategory;

public final class LevelUpToast implements Toast {
    private static final Object TOKEN = new Object();
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("codex", "textures/gui/sprites/skill_toast.png");
    private static final long DISPLAY_TIME_MS = 5000L;

    private Component title;
    private Component subtitle;
    private long lastChanged;
    private boolean changed;

    private LevelUpToast(int newLevel, int levelsGained) {
        update(newLevel, levelsGained);
    }

    public static void show(int newLevel, int levelsGained) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        ToastComponent toasts = mc.getToasts();
        LevelUpToast existing = toasts.getToast(LevelUpToast.class, TOKEN);
        if (existing != null) {
            existing.update(newLevel, levelsGained);
            return;
        }

        toasts.addToast(new LevelUpToast(newLevel, levelsGained));
    }

    @Override
    public Visibility render(GuiGraphics gg, ToastComponent component, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }

        gg.blit(TEXTURE, 0, 0, 0, 0, width(), height(), width(), height());
        gg.renderItem(new ItemStack(SkillCategory.STRENGTH.icon()), 6, 6);
        gg.drawString(Minecraft.getInstance().font, title, 30, 7, 0x242424, false);
        gg.drawString(Minecraft.getInstance().font, subtitle, 30, 18, 0x8f8f8f, false);
        return time - lastChanged >= DISPLAY_TIME_MS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public Object getToken() {
        return TOKEN;
    }

    @Override
    public int width() {
        return 160;
    }

    @Override
    public int height() {
        return 32;
    }

    private void update(int newLevel, int levelsGained) {
        title = Component.translatable("toast.codex.level_up");
        subtitle = levelsGained > 1
                ? Component.translatable("toast.codex.level_up.multi", levelsGained, newLevel)
                : Component.translatable("toast.codex.level_up.single", newLevel);
        changed = true;
    }
}
