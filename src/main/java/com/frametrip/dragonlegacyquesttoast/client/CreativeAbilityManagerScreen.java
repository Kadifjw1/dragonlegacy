package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.ToggleAbilityPacket;
import com.frametrip.dragonlegacyquesttoast.server.AbilityDefinition;
import com.frametrip.dragonlegacyquesttoast.server.AbilityRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
 
import java.util.List;
 
public class CreativeAbilityManagerScreen extends Screen {
 
    private static final int W  = 400;
    private static final int H  = 285;
    private static final int ROW_H = 22;
 
    private static final int[] PATH_COLORS = {0xFFFF5500, 0xFF44AAFF, 0xFFFFDD00, 0xFFAA44FF};
    private static final AwakeningPathType[] PATHS = AwakeningPathType.values();
 
    private final Screen parent;
    private AwakeningPathType activeTab = AwakeningPathType.FIRE;
 
    public CreativeAbilityManagerScreen(Screen parent) {
        super(Component.literal("Управление способностями"));
        this.parent = parent;
    }
 
    @Override
    protected void init() {
        super.init();
        // Нет фиксированных виджетов — всё рисуется и обрабатывается вручную
    }
 
    @Override
    public boolean isPauseScreen() { return false; }
 
    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }
 
    // ── Render ────────────────────────────────────────────────────────────────
 
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
 
        // Фон
        g.fill(ox, oy, ox + W, oy + H, 0xDD0E0E18);
        drawBorder(g, ox, oy, W, H, 0xFF444455);
 
        // Заголовок
        g.fill(ox, oy, ox + W, oy + 22, 0xBB161622);
        drawBorder(g, ox, oy, W, 22, 0xFF333355);
        g.drawCenteredString(font, "Управление способностями (Creative)", ox + W / 2, oy + 7, 0xFFE6D7B5);
 
        // Вкладки
        int tabW = W / 4;
        for (int i = 0; i < PATHS.length; i++) {
            boolean active = PATHS[i] == activeTab;
            int tx = ox + tabW * i;
            int ty = oy + 22;
            g.fill(tx, ty, tx + tabW, ty + 20, active ? 0xBB1A1A3A : 0x66111122);
            drawBorder(g, tx, ty, tabW, 20, active ? PATH_COLORS[i] : 0xFF333344);
            g.drawCenteredString(font, PATHS[i].getTitle(), tx + tabW / 2, ty + 6, active ? PATH_COLORS[i] : 0xFF888888);
        }
 
        // Список способностей
        List<AbilityDefinition> abilities = AbilityRegistry.getForPath(activeTab);
        int listY = oy + 46;
        int listX = ox + 10;
        int pathColor = PATH_COLORS[activeTab.ordinal()];
 
        for (int i = 0; i < abilities.size(); i++) {
            AbilityDefinition def = abilities.get(i);
            int rowY = listY + i * ROW_H;
            boolean enabled = ClientPlayerAbilityState.hasAbility(def.id);
 
            g.fill(listX, rowY, ox + W - 10, rowY + ROW_H - 2,
                   enabled ? 0x33001100 : 0x22110000);
            drawBorder(g, listX, rowY, W - 20, ROW_H - 2,
                       enabled ? 0x66334433 : 0x44333333);
 
            String tierStr = def.tier == 6 ? "★" : String.valueOf(def.tier);
            g.drawString(font, "[" + tierStr + "] " + def.name,
                         listX + 6, rowY + 6,
                         enabled ? 0xFFDDDDDD : 0xFF666666, false);
 
            // Кнопка переключения
            int bx = ox + W - 74, by = rowY + 3, bw = 60, bh = ROW_H - 8;
            boolean hovered = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            g.fill(bx, by, bx + bw, by + bh, enabled ? 0xFF1A3A1A : 0xFF3A1A1A);
            drawBorder(g, bx, by, bw, bh,
                       hovered ? 0xFFFFFFFF : (enabled ? 0xFF44AA44 : 0xFFAA4444));
            g.drawCenteredString(font, enabled ? "ВКЛ" : "ВЫКЛ", bx + bw / 2, by + 3,
                                 enabled ? 0xFF44FF44 : 0xFFFF4444);
        }
 
        // Нижняя строка статуса
        int statY = oy + H - 42;
        g.fill(ox + 10, statY, ox + W - 10, statY + 14, 0x44111133);
        long active = abilities.stream().filter(d -> ClientPlayerAbilityState.hasAbility(d.id)).count();
        g.drawCenteredString(font, "Активно: " + active + " из " + abilities.size(),
                             ox + W / 2, statY + 3, 0xFFAAAAAA);
 
        // Кнопка «Назад»
        int bbx = ox + W / 2 - 40, bby = oy + H - 24, bbw = 80, bbh = 20;
        boolean backHover = mx >= bbx && mx < bbx + bbw && my >= bby && my < bby + bbh;
        g.fill(bbx, bby, bbx + bbw, bby + bbh, backHover ? 0xFF333355 : 0xFF222233);
        drawBorder(g, bbx, bby, bbw, bbh, 0xFF555577);
        g.drawCenteredString(font, "Назад", bbx + bbw / 2, bby + 6, 0xFFCCCCCC);
    }
 
    // ── Input ─────────────────────────────────────────────────────────────────
 
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
 
        // Вкладки
        int tabW = W / 4;
        for (int i = 0; i < PATHS.length; i++) {
            int tx = ox + tabW * i, ty = oy + 22;
            if (mx >= tx && mx < tx + tabW && my >= ty && my < ty + 20) {
                activeTab = PATHS[i];
                return true;
            }
        }
 
        // Кнопки вкл/выкл
        List<AbilityDefinition> abilities = AbilityRegistry.getForPath(activeTab);
        int listY = oy + 46;
 
        for (int i = 0; i < abilities.size(); i++) {
            AbilityDefinition def = abilities.get(i);
            int rowY = listY + i * ROW_H;
            int bx = ox + W - 74, by = rowY + 3, bw = 60, bh = ROW_H - 8;
 
            if (mx >= bx && mx < bx + bw && my >= by && my < by + bh) {
                boolean wasEnabled = ClientPlayerAbilityState.hasAbility(def.id);
                ModNetwork.CHANNEL.sendToServer(new ToggleAbilityPacket(def.id, !wasEnabled));
                return true;
            }
        }
 
        // Кнопка «Назад»
        int bbx = ox + W / 2 - 40, bby = oy + H - 24;
        if (mx >= bbx && mx < bbx + 80 && my >= bby && my < bby + 20) {
            onClose();
            return true;
        }
 
        return super.mouseClicked(mx, my, btn);
    }
 
    // ── Utils ─────────────────────────────────────────────────────────────────
 
    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,         y,         x + w, y + 1,     c);
        g.fill(x,         y + h - 1, x + w, y + h,     c);
        g.fill(x,         y,         x + 1, y + h,     c);
        g.fill(x + w - 1, y,         x + w, y + h,     c);
    }
}
