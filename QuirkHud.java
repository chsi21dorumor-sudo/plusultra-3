package com.plusultra.client;

import com.plusultra.config.PUConfig;
import com.plusultra.mission.Ranks;
import com.plusultra.quirk.Quirk;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Top-left HUD: quirk name and level, stamina and XP bars, ability list with cooldowns, mission line. */
public final class QuirkHud {
    private QuirkHud() {}

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        Quirk q = Quirk.byId(ClientData.quirk);
        if (q == null) return;

        int x = 8, y = 8;
        int color = 0xFF000000 | q.color();

        g.drawString(mc.font, Component.translatable("quirk.plusultra." + q.id())
                .append(Component.literal("  Lv." + ClientData.level)), x, y, color, true);
        if (!ClientData.fusionQuirk.isEmpty()) {
            y += 10;
            g.drawString(mc.font, Component.literal("Fusion: ").append(Component.translatable("quirk.plusultra." + ClientData.fusionQuirk)), x, y, 0xFFB66DFF, true);
        }
        y += 12;

        // stamina bar
        int w = 110;
        float frac = ClientData.maxStamina <= 0 ? 0 : Math.min(1f, ClientData.stamina / ClientData.maxStamina);
        g.fill(x - 1, y - 1, x + w + 1, y + 7, 0xAA000000);
        g.fill(x, y, x + (int) (w * frac), y + 6, color);
        y += 9;

        // xp bar
        float xpFrac = ClientData.xpNeeded <= 0 ? 0 : Math.min(1f, (float) ClientData.xp / ClientData.xpNeeded);
        g.fill(x - 1, y - 1, x + w + 1, y + 3, 0xAA000000);
        g.fill(x, y, x + (int) (w * xpFrac), y + 2, 0xFFFFD700);
        y += 8;

        // abilities
        for (int i = 0; i < 3; i++) {
            MutableComponent line = Component.literal("[")
                    .append(ClientKeys.ABILITY[i].getTranslatedKeyMessage())
                    .append(Component.literal("] "))
                    .append(Component.translatable("ability.plusultra." + q.id() + "." + q.abilities()[i].id()));
            int lineColor = 0xFFFFFFFF;
            if (i == 2 && ClientData.level < PUConfig.ULT_LEVEL.get()) {
                line.append(Component.literal("  (Lv." + PUConfig.ULT_LEVEL.get() + ")"));
                lineColor = 0xFF777777;
            } else if (ClientData.cooldownTicks(i) > 0) {
                line.append(Component.literal(String.format("  %.1fs", ClientData.cooldownTicks(i) / 20.0)));
                lineColor = 0xFFAAAAAA;
            }
            g.drawString(mc.font, line, x, y, lineColor, true);
            y += 10;
        }

        // rank + mission
        y += 2;
        g.drawString(mc.font, "Skill Points: " + ClientData.skillPoints + "   [V] Evolution",
                x, y, 0xFF55FFFF, true);
        y += 10;
        g.drawString(mc.font, Ranks.of(ClientData.points).name() + " - " + ClientData.points + " pts",
                x, y, 0xFFFFD700, true);
        if (!ClientData.mission.isEmpty()) {
            y += 10;
            String txt = switch (ClientData.mission) {
                case "patrol" -> "Patrol " + ClientData.missionProgress + "/" + ClientData.missionGoal;
                case "villain" -> "Capture the villain";
                default -> "";
            };
            g.drawString(mc.font, txt, x, y, 0xFFFFFF55, true);
        }
    }
}
