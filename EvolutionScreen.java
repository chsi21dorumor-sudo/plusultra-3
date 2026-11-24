package com.plusultra.client;

import com.plusultra.PlusUltra;
import com.plusultra.net.EvolutionPayload;
import com.plusultra.quirk.Quirk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Quirk Evolution screen: six linked talents and a simple fusion selector. */
public final class EvolutionScreen extends Screen {
    private static final String[] NAMES = {"Power", "Efficiency", "Cooldown", "Range", "Technique", "Mastery"};
    private static final String[] DESC = {"+10% ability power", "-10% stamina cost", "-10% cooldown", "+10% ability range", "+5% overall power", "+5% overall power"};

    public EvolutionScreen() { super(Component.literal("Quirk Evolution")); }

    @Override protected void init() {
        int left = this.width / 2 - 150;
        for (int i = 0; i < 6; i++) {
            final int index = i;
            int col = i < 3 ? 0 : 1;
            int row = i % 3;
            int x = left + col * 155;
            int y = 55 + row * 38;
            String state = ClientData.hasTalent(index) ? "✓ " : "";
            Button b = Button.builder(Component.literal(state + NAMES[i]), btn -> {
                PacketDistributor.sendToServer(new EvolutionPayload(0, index));
            }).bounds(x, y, 145, 20).build();
            addRenderableWidget(b);
        }

        int fy = 180;
        Quirk[] qs = Quirk.values();
        int ordinal = 0;
        for (Quirk q : qs) {
            final int pick = ordinal++;
            if (q.id().equals(ClientData.quirk)) continue;
            Button b = Button.builder(Component.literal("Fuse: " + q.name().replace('_', ' ')), btn -> {
                PacketDistributor.sendToServer(new EvolutionPayload(1, pick));
                onClose();
            }).bounds(left, fy, 300, 20).build();
            addRenderableWidget(b);
            fy += 23;
        }
        if (!ClientData.fusionQuirk.isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal("Remove Fusion"), btn -> {
                PacketDistributor.sendToServer(new EvolutionPayload(2, 0));
                onClose();
            }).bounds(left, fy + 5, 300, 20).build());
        }
    }

    public static boolean hasTalent(int i) { return i >= 0 && i < 6 && (ClientData.talents & (1L << i)) != 0; }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        int left = this.width / 2 - 150;
        g.drawCenteredString(this.font, Component.literal("QUIRK EVOLUTION"), this.width / 2, 18, 0xFFFFFFFF);
        g.drawCenteredString(this.font, Component.literal("Skill Points: " + ClientData.skillPoints), this.width / 2, 33, 0xFF55FFFF);
        for (int i = 0; i < 6; i++) {
            int col = i < 3 ? 0 : 1;
            int row = i % 3;
            int x = left + col * 155;
            int y = 78 + row * 38;
            g.drawString(this.font, DESC[i], x, y, 0xFFAAAAAA, false);
        }
        if (ClientData.level < 5) {
            g.drawCenteredString(this.font, Component.literal("Fusion unlocks at level 5 and costs 3 skill points."), this.width / 2, this.height - 18, 0xFFFF7777);
        }
    }
}
