package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.UnlockAbilityPacket;
import com.frametrip.dragonlegacyquesttoast.server.AbilityDefinition;
import com.frametrip.dragonlegacyquesttoast.server.AbilityRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
 
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
 
import java.util.ArrayList;
import java.util.List;

public class AwakeningPathDetailScreen extends Screen {
    
     // ── Textures ──────────────────────────────────────────────────────────────
    
private static final ResourceLocation FIRE_BG =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_fire_bg_320x220.png");
    private static final ResourceLocation ICE_BG =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_ice_bg_320x220.png");
    private static final ResourceLocation STORM_BG =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_storm_bg_320x220.png");
      private static final ResourceLocation VOID_BG =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_void_bg_320x220.png");
    
        // ── Tree layout (x, y) relative to bgX/bgY — 10 nodes, 24×24 each ───────
 
    private static final int NODE_W = 24;
    private static final int[][] NODE_POS = {
            {148, 28},   // 0  tier 1
            {68,  60},   // 1  tier 2 left
            {228, 60},   // 2  tier 2 right
            {68,  93},   // 3  tier 3 left
            {228, 93},   // 4  tier 3 right
            {68,  126},  // 5  tier 4 left
            {228, 126},  // 6  tier 4 right
            {104, 157},  // 7  tier 5 left  (converging)
            {192, 157},  // 8  tier 5 right (converging)
            {148, 188},  // 9  ultimate
    };
 
    // pairs of node-indices that should be connected by a line
    private static final int[][] CONNECTIONS = {
            {0,1},{0,2},
            {1,3},{2,4},
            {3,5},{4,6},
            {5,7},{6,8},
            {7,9},{8,9},
    };
 
    // ── Fields ────────────────────────────────────────────────────────────────
 
    private final Screen parent;
    private final AwakeningPathType pathType;
     private final List<AbilityDefinition> abilities;
 
    // treeIndex-0 of the node the mouse is hovering over (-1 = none)
    private int hoveredNode = -1;

    public AwakeningPathDetailScreen(Screen parent, AwakeningPathType pathType) {
        super(Component.literal(pathType.getTitle()));
        this.parent = parent;
        this.pathType = pathType;
        this.abilities = AbilityRegistry.getForPath(pathType); // sorted by treeIndex 1-10
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
         addRenderableWidget(Button.builder(Component.literal("Назад"), b -> {
            if (minecraft != null) minecraft.setScreen(parent);
        }).bounds(width / 2 - 40, height - 30, 80, 20).build());
        
        if (canEdit()) {
        addRenderableWidget(Button.builder(Component.literal("Ред"), b -> {
                if (minecraft != null) minecraft.setScreen(new AwakeningPathEditorScreen(parent, pathType));
            }).bounds(8, 8, 40, 20).build());
        }
    }

    private boolean canEdit() {
        return minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild;
    }

    private boolean isCreative() {
        return minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild;
    }

@Override public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    // ── Render ────────────────────────────────────────────────────────────────
    
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);

        int bgX = ClientAwakeningPathScreenState.getBgX(pathType);
        int bgY = ClientAwakeningPathScreenState.getBgY(pathType);
        int bgW = ClientAwakeningPathScreenState.getBgWidth(pathType);
        int bgH = ClientAwakeningPathScreenState.getBgHeight(pathType);

        RenderSystem.enableBlend();
        g.blit(getBg(), bgX, bgY, 0, 0, bgW, bgH, bgW, bgH);
 
        // Title
        g.drawCenteredString(font, pathType.getTitle(), width / 2, bgY + 8, pathColor(0xFFE6D7B5));
 
        // Points (survival only)
        if (!isCreative()) {
            String pts = "Очки: " + ClientPlayerAbilityState.getPoints();
            g.drawString(font, pts, bgX + bgW - font.width(pts) - 8, bgY + 8, 0xFFD4AA44, false);
        }
 
        // Detect hovered node
        hoveredNode = -1;
        for (int i = 0; i < NODE_POS.length; i++) {
            int nx = bgX + NODE_POS[i][0], ny = bgY + NODE_POS[i][1];
            if (mouseX >= nx && mouseX < nx + NODE_W && mouseY >= ny && mouseY < ny + NODE_W) {
                hoveredNode = i;
                break;
            }
        }
 
        // Connection lines
        for (int[] conn : CONNECTIONS) {
            int ai = conn[0], bi = conn[1];
            int ax = bgX + NODE_POS[ai][0] + NODE_W/2;
            int ay = bgY + NODE_POS[ai][1] + NODE_W/2;
            int bx = bgX + NODE_POS[bi][0] + NODE_W/2;
            int by = bgY + NODE_POS[bi][1] + NODE_W/2;
            AbilityDefinition defA = abilityAt(ai);
            AbilityDefinition defB = abilityAt(bi);
            boolean active = isUnlocked(defA) && isUnlocked(defB);
            drawLine(g, ax, ay, bx, by, active ? (pathColor(0xCCFFFFFF) & 0xBBFFFFFF) : 0xFF333333);
        }
 
        // Nodes
        for (int i = 0; i < NODE_POS.length; i++) {
            renderNode(g, i, bgX, bgY, i == hoveredNode);
        }
 
        // Widgets (buttons) on top
        super.render(g, mouseX, mouseY, pt);
 
        // Tooltip last (above everything)
        if (hoveredNode >= 0) {
            renderNodeTooltip(g, hoveredNode, mouseX, mouseY);
        }
    }
 
    // ── Node rendering ────────────────────────────────────────────────────────
 
    private void renderNode(GuiGraphics g, int idx, int bgX, int bgY, boolean hovered) {
        int nx = bgX + NODE_POS[idx][0];
        int ny = bgY + NODE_POS[idx][1];
        AbilityDefinition def = abilityAt(idx);
        if (def == null) return;
 
        boolean unlocked  = isUnlocked(def);
        boolean canUnlock = !unlocked && canUnlock(def);
        boolean isUltimate = def.tier == 6;
 
        // Background fill
        int fillColor;
        if (unlocked) {
            fillColor = hovered ? brighter(pathNodeColor(), 40) : pathNodeColor();
        } else if (canUnlock) {
            fillColor = hovered ? 0xFF554433 : 0xFF443322;
        } else {
            fillColor = 0xFF222222;
        }
        g.fill(nx + 1, ny + 1, nx + NODE_W - 1, ny + NODE_W - 1, fillColor);
 
        // Border
        int border = unlocked ? pathBorderColor() : (canUnlock ? 0xFF887755 : 0xFF444444);
        if (isUltimate && unlocked) border = 0xFFFFDD44; // golden for unlocked ultimate
        if (hovered) border = brighter(border, 30);
        drawBorder(g, nx, ny, NODE_W, NODE_W, border);
 
        // Icon inside node (small layered rects simulating path symbol)
        drawNodeIcon(g, nx, ny, idx, unlocked);
 
        // Tier number bottom-right corner (tiny)
        String tierStr = def.tier == 6 ? "★" : String.valueOf(def.tier);
        g.drawString(font, tierStr, nx + NODE_W - font.width(tierStr) - 2, ny + NODE_W - 9,
                unlocked ? pathBorderColor() : 0xFF666666, false);
    }
 
    private void drawNodeIcon(GuiGraphics g, int nx, int ny, int idx, boolean unlocked) {
        int cx = nx + NODE_W / 2;
        int cy = ny + NODE_W / 2;
        int c1 = unlocked ? pathNodeColor() : 0xFF444444;
        int c2 = unlocked ? brighter(pathNodeColor(), 60) : 0xFF555555;
        // Unique small icon per tier (geometric shapes inside the node)
        switch (idx % 4) {
            case 0 -> { // diamond
                g.fill(cx - 1, cy - 4, cx + 1, cy - 2, c2);
                g.fill(cx - 3, cy - 2, cx + 3, cy,     c1);
                g.fill(cx - 3, cy,     cx + 3, cy + 2, c1);
                g.fill(cx - 1, cy + 2, cx + 1, cy + 4, c2);
            }
            case 1 -> { // cross
                g.fill(cx - 1, cy - 4, cx + 1, cy + 4, c1);
                g.fill(cx - 4, cy - 1, cx + 4, cy + 1, c2);
            }
            case 2 -> { // triangle up
                for (int row = 0; row < 5; row++) {
                    g.fill(cx - row, cy + row - 2, cx + row, cy + row - 1, row == 0 ? c2 : c1);
                }
            }
            case 3 -> { // circle
                g.fill(cx - 2, cy - 3, cx + 2, cy + 3, c1);
                g.fill(cx - 3, cy - 2, cx + 3, cy + 2, c1);
                g.fill(cx - 1, cy - 2, cx + 1, cy + 2, c2);
            }
        }
    }
 
    // ── Tooltip ───────────────────────────────────────────────────────────────
 
    private void renderNodeTooltip(GuiGraphics g, int idx, int mouseX, int mouseY) {
        AbilityDefinition def = abilityAt(idx);
        if (def == null) return;

        boolean unlocked  = isUnlocked(def);
        boolean creative  = isCreative();
        boolean canUnlock = !unlocked && !creative && canUnlock(def);

         List<Component> lines = new ArrayList<>();
        int tierColor = def.tier == 6 ? 0xFFDD44 : pathTooltipColor();
        lines.add(Component.literal(def.name).withStyle(s -> s.withColor(tierColor)));
        lines.add(Component.literal("Уровень " + (def.tier == 6 ? "✦ ВЫСШИЙ" : def.tier))
                .withStyle(s -> s.withColor(0xAAAAAA)));
        lines.add(Component.empty());
        // description wrapped by chars
        wrapText(def.description, 38).forEach(line ->
                lines.add(Component.literal(line).withStyle(s -> s.withColor(0xCCCCCC))));
        lines.add(Component.empty());

        if (creative || unlocked) {
            lines.add(Component.literal(unlocked ? "✔ Активна" : "✔ Доступна (Creative)")
                    .withStyle(s -> s.withColor(0x44FF44)));
        } else {
            lines.add(Component.literal("Стоимость: " + def.cost + " очк. пробуждения")
                    .withStyle(s -> s.withColor(0xFFCC44)));
            if (!def.requires.isEmpty()) {
                StringBuilder req = new StringBuilder("Требует: ");
                for (String r : def.requires) {
                    AbilityDefinition rd = AbilityRegistry.get(r);
                    req.append(rd != null ? rd.name : r).append(", ");
                }
                lines.add(Component.literal(req.substring(0, req.length() - 2))
                        .withStyle(s -> s.withColor(0xAAAAAA)));
            }
            if (canUnlock) {
                lines.add(Component.literal("[Нажми, чтобы открыть]")
                        .withStyle(s -> s.withColor(0x88FFFF)));
            } else if (!requirementsMet(def)) {
                lines.add(Component.literal("✘ Требования не выполнены")
                        .withStyle(s -> s.withColor(0xFF4444)));
            } else {
                lines.add(Component.literal("✘ Недостаточно очков")
                        .withStyle(s -> s.withColor(0xFF4444)));
            }
        }
 
        g.renderComponentTooltip(font, lines, mouseX, mouseY);
    }
 
    // ── Mouse click ───────────────────────────────────────────────────────────
 
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredNode >= 0) {
            AbilityDefinition def = abilityAt(hoveredNode);
            if (def != null && !isUnlocked(def) && !isCreative() && canUnlock(def)) {
                ModNetwork.CHANNEL.sendToServer(new UnlockAbilityPacket(def.id));
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                            Component.literal("Открываем: " + def.name + "..."), true);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
 
    // ── Path helpers ──────────────────────────────────────────────────────────
 
    private ResourceLocation getBg() {
        return switch (pathType) {
            case FIRE  -> FIRE_BG;
            case ICE   -> ICE_BG;
            case STORM -> STORM_BG;
            case VOID  -> VOID_BG;
        };
    }
 
    private int pathNodeColor() {
        return switch (pathType) {
            case FIRE  -> 0xFF882200;
            case ICE   -> 0xFF1155AA;
            case STORM -> 0xFF887700;
            case VOID  -> 0xFF440088;
        };
    }
 
    private int pathBorderColor() {
        return switch (pathType) {
            case FIRE  -> 0xFFFF6600;
            case ICE   -> 0xFF44AAFF;
            case STORM -> 0xFFFFDD00;
            case VOID  -> 0xFFAA44FF;
        };
    }
 
    private int pathTooltipColor() {
        return switch (pathType) {
            case FIRE  -> 0xFFAA44;
            case ICE   -> 0x44CCFF;
            case STORM -> 0xFFEE44;
            case VOID  -> 0xBB66FF;
        };
    }
 
    private int pathColor(int base) { return base; }
 
    // ── State queries ─────────────────────────────────────────────────────────
 
    private boolean isUnlocked(AbilityDefinition def) {
        if (def == null) return false;
        return isCreative() || ClientPlayerAbilityState.hasAbility(def.id);
    }
 
    private boolean requirementsMet(AbilityDefinition def) {
        for (String req : def.requires) {
            if (!ClientPlayerAbilityState.hasAbility(req)) return false;
        }
        return true;
    }
 
    private boolean canUnlock(AbilityDefinition def) {
        return requirementsMet(def) && ClientPlayerAbilityState.getPoints() >= def.cost;
    }
 
    private AbilityDefinition abilityAt(int nodeIdx) {
        // nodeIdx 0-9 maps to treeIndex 1-10
        int ti = nodeIdx + 1;
        return abilities.stream().filter(d -> d.treeIndex == ti).findFirst().orElse(null);
    }
 
    // ── Drawing helpers ───────────────────────────────────────────────────────
 
    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color);
        g.fill(x,         y + h - 1, x + w,     y + h,     color);
        g.fill(x,         y,         x + 1,     y + h,     color);
        g.fill(x + w - 1, y,         x + w,     y + h,     color);
    }
 
    private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = x2 - x1, dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            int x = Math.round(x1 + (float) dx * i / steps);
            int y = Math.round(y1 + (float) dy * i / steps);
            g.fill(x, y, x + 1, y + 1, color);
            g.fill(x + 1, y, x + 2, y + 1, color & 0x77FFFFFF); // slight width
        }
    }
 
    private static int brighter(int color, int amount) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int gr = Math.min(255, ((color >> 8)  & 0xFF) + amount);
        int b  = Math.min(255, ( color        & 0xFF) + amount);
        return (color & 0xFF000000) | (r << 16) | (gr << 8) | b;
    }
 
    private static List<String> wrapText(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            if (cur.length() + w.length() + 1 > maxChars) {
                if (cur.length() > 0) { lines.add(cur.toString()); cur = new StringBuilder(); }
            }
            if (cur.length() > 0) cur.append(' ');
            cur.append(w);
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }
}
