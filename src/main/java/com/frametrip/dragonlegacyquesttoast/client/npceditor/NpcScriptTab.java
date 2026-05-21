package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcEntityDataPacket;
import com.frametrip.dragonlegacyquesttoast.server.event.EventChain;
import com.frametrip.dragonlegacyquesttoast.server.script.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

// [SCR-2]: Visual node-graph editor for NPC scripts. Compiles to EventChain on save.
public class NpcScriptTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF9966CC;

    // Canvas geometry
    private static final int CANVAS_H  = 280;
    private static final int NODE_W    = 120;
    private static final int NODE_H    = 48;
    private static final int HEADER_H  = 14;
    private static final int PORT_R    = 5;  // port dot radius
    private static final int GRAPH_LIST_W = 130;

    // Pan state
    private float panX = 0f, panY = 0f;
    private boolean panning    = false;
    private double  panStartX  = 0, panStartY = 0;
    private float   panStartPX = 0f, panStartPY = 0f;

    // Node drag
    private ScriptNode dragNode    = null;
    private float      dragOffsetX = 0f;
    private float      dragOffsetY = 0f;

    // Wire: pending output port → waiting for input click
    private ScriptNode wireSrc = null;

    // Current graph index
    private int graphIndex = 0;

    // Graph name edit box
    private EditBox graphNameBox;
    // Node param edit box (shown for selected node)
    private EditBox nodeParamBox;
    private ScriptNode selectedNode = null;

    // Cached layout values set in init()
    private int canvasX, canvasY, canvasW;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        Font font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        if (d.scriptGraphs == null) d.scriptGraphs = new ArrayList<>();

        canvasX = rx + GRAPH_LIST_W + 2;
        canvasY = oy + 20;
        canvasW = rw - GRAPH_LIST_W - 4;

        // Graph name box at the top
        graphNameBox = new EditBox(font, rx + GRAPH_LIST_W + 4, oy + 2, canvasW - 60, 14,
                Component.literal("Имя графа"));
        graphNameBox.setMaxLength(40);
        ScriptGraph cur = currentGraph(d);
        if (cur != null) graphNameBox.setValue(cur.name);
        add.accept(graphNameBox);

        // Node parameter box (below canvas)
        int paramY = oy + 20 + CANVAS_H + 4;
        nodeParamBox = new EditBox(font, canvasX + 2, paramY, canvasW - 80, 14,
                Component.literal("Параметр"));
        nodeParamBox.setMaxLength(64);
        if (selectedNode != null) nodeParamBox.setValue(selectedNode.paramValue);
        add.accept(nodeParamBox);

        // Add-graph button
        add.accept(Button.builder(Component.literal("+Граф"), b -> {
            ScriptGraph g = new ScriptGraph();
            g.name = "Граф " + (d.scriptGraphs.size() + 1);
            d.scriptGraphs.add(g);
            graphIndex = d.scriptGraphs.size() - 1;
            rebuild.run();
        }).bounds(rx, oy + 2, GRAPH_LIST_W - 2, 14).build());

        // Delete-graph button
        add.accept(Button.builder(Component.literal("✕Граф"), b -> {
            if (!d.scriptGraphs.isEmpty()) {
                d.scriptGraphs.remove(graphIndex);
                graphIndex = Math.max(0, graphIndex - 1);
                rebuild.run();
            }
        }).bounds(rx, oy + 18, GRAPH_LIST_W - 2, 14).build());

        // Add-node buttons (one per type)
        ScriptNodeType[] types = ScriptNodeType.values();
        for (int i = 0; i < types.length; i++) {
            final ScriptNodeType t = types[i];
            add.accept(Button.builder(Component.literal("+" + t.label), b -> {
                ScriptGraph g = currentGraph(d);
                if (g == null) return;
                ScriptNode n = new ScriptNode();
                n.id   = UUID.randomUUID().toString().substring(0, 8);
                n.type = t;
                n.posX = 20 - panX;
                n.posY = 20 + i * 60 - panY;
                g.nodes.add(n);
            }).bounds(rx, oy + 36 + i * 18, GRAPH_LIST_W - 2, 14).build());
        }

        // Delete selected node
        add.accept(Button.builder(Component.literal("✕Узел"), b -> {
            ScriptGraph g = currentGraph(d);
            if (g == null || selectedNode == null) return;
            g.nodes.remove(selectedNode);
            // Remove all wires pointing to this node
            for (ScriptNode n : g.nodes) n.outputTo.remove(selectedNode.id);
            selectedNode = null;
            nodeParamBox.setValue("");
        }).bounds(rx, oy + 36 + types.length * 18, GRAPH_LIST_W - 2, 14).build());

        // Compile & save button
        int saveY = oy + 20 + CANVAS_H + 4;
        add.accept(Button.builder(Component.literal("▶ Скомп. и сохр."), b -> {
            pullFields(state);
            compileAndSave(state);
        }).bounds(canvasX + canvasW - 76, saveY, 76, 16).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        NpcEntityData d = state.getDraft();
        Font font = Minecraft.getInstance().font;

        // ── Graph list sidebar ──────────────────────────────────────────────────
        int listX = rx;
        int listY = oy + 36 + ScriptNodeType.values().length * 18 + 20;
        g.fill(listX, oy + 36, listX + GRAPH_LIST_W - 2, oy + 36 + ScriptNodeType.values().length * 18,
                0x22FFFFFF);
        for (int i = 0; d.scriptGraphs != null && i < d.scriptGraphs.size(); i++) {
            int rowY = listY + i * 12;
            int bg = (i == graphIndex) ? 0xFF5544AA : 0x44FFFFFF;
            g.fill(listX, rowY, listX + GRAPH_LIST_W - 2, rowY + 11, bg);
            g.drawString(font, d.scriptGraphs.get(i).name, listX + 2, rowY + 2, 0xFFFFFFFF, false);
        }

        // ── Canvas area ─────────────────────────────────────────────────────────
        g.fill(canvasX, canvasY, canvasX + canvasW, canvasY + CANVAS_H, 0xFF1A1A2A);
        g.enableScissor(canvasX, canvasY, canvasX + canvasW, canvasY + CANVAS_H);

        ScriptGraph graph = currentGraph(d);
        if (graph != null) {
            // Draw wires first (below nodes)
            for (ScriptNode src : graph.nodes) {
                for (String destId : src.outputTo) {
                    ScriptNode dest = graph.findById(destId);
                    if (dest != null) drawBezier(g, src, dest);
                }
            }
            // Draw pending wire to mouse
            if (wireSrc != null) {
                float ox1 = canvasX + panX + wireSrc.posX + NODE_W;
                float oy1 = canvasY + panY + wireSrc.posY + NODE_H / 2f;
                drawBezierRaw(g, ox1, oy1, mx, my, 0xFFFFFFAA);
            }
            // Draw nodes
            for (ScriptNode node : graph.nodes) {
                drawNode(g, font, node, node == selectedNode, mx, my);
            }
        }

        g.disableScissor();

        // Param bar label
        int paramY = oy + 20 + CANVAS_H + 4;
        if (selectedNode != null) {
            String label = selectedNode.type.label + ": " + (selectedNode.subType.isEmpty() ? "—" : selectedNode.subType);
            g.drawString(font, label, canvasX + 2, paramY - 10, 0xFFCCCCCC, false);
        }
        g.drawString(font, "§7[Скрипты]", rx, oy, 0xFFFFFFFF, false);
    }

    private void drawNode(GuiGraphics g, Font font, ScriptNode node,
                          boolean selected, int mx, int my) {
        int nx = (int)(canvasX + panX + node.posX);
        int ny = (int)(canvasY + panY + node.posY);

        int headerCol = node.type.headerColor;
        int bodyCol   = node.type.bodyColor;
        if (selected) {
            headerCol = blend(headerCol, 0xFFFFFFFF, 0.25f);
            bodyCol   = blend(bodyCol,   0xFFFFFFFF, 0.15f);
        }

        // Shadow
        g.fill(nx + 2, ny + 2, nx + NODE_W + 2, ny + NODE_H + 2, 0x66000000);
        // Header
        g.fill(nx, ny, nx + NODE_W, ny + HEADER_H, headerCol);
        // Body
        g.fill(nx, ny + HEADER_H, nx + NODE_W, ny + NODE_H, bodyCol);
        // Border
        g.fill(nx, ny, nx + NODE_W, ny + 1, 0xFFFFFFFF);
        g.fill(nx, ny + NODE_H - 1, nx + NODE_W, ny + NODE_H, 0xFFFFFFFF);
        g.fill(nx, ny, nx + 1, ny + NODE_H, 0xFFFFFFFF);
        g.fill(nx + NODE_W - 1, ny, nx + NODE_W, ny + NODE_H, 0xFFFFFFFF);

        // Header label
        g.drawString(font, node.type.label, nx + 3, ny + 3, 0xFFFFFFFF, false);

        // Sub-type label
        String sub = node.subType.isEmpty() ? "—" : node.subType.toLowerCase();
        if (sub.length() > 14) sub = sub.substring(0, 13) + "…";
        g.drawString(font, sub, nx + 3, ny + HEADER_H + 3, 0xFFDDDDDD, false);

        // Param value (second line)
        if (!node.paramValue.isEmpty()) {
            String pv = node.paramValue;
            if (pv.length() > 14) pv = pv.substring(0, 13) + "…";
            g.drawString(font, "§7" + pv, nx + 3, ny + HEADER_H + 14, 0xFFAAAAAA, false);
        }

        // Input port (left middle)
        if (node.type != ScriptNodeType.EVENT) {
            int px = nx;
            int py = ny + NODE_H / 2;
            g.fill(px - PORT_R, py - PORT_R, px + PORT_R, py + PORT_R, 0xFF88CCFF);
        }

        // Output port (right middle)
        if (node.type != ScriptNodeType.OUTPUT) {
            int px = nx + NODE_W;
            int py = ny + NODE_H / 2;
            int portCol = (wireSrc == node) ? 0xFFFFFF00 : 0xFFFFCC44;
            g.fill(px - PORT_R, py - PORT_R, px + PORT_R, py + PORT_R, portCol);
        }
    }

    private void drawBezier(GuiGraphics g, ScriptNode src, ScriptNode dest) {
        float x0 = canvasX + panX + src.posX  + NODE_W;
        float y0 = canvasY + panY + src.posY  + NODE_H / 2f;
        float x1 = canvasX + panX + dest.posX;
        float y1 = canvasY + panY + dest.posY + NODE_H / 2f;
        drawBezierRaw(g, x0, y0, x1, y1, 0xFFFFCC44);
    }

    private static void drawBezierRaw(GuiGraphics g, float x0, float y0, float x1, float y1, int col) {
        float cx0 = x0 + (x1 - x0) * 0.5f;
        float cx1 = x0 + (x1 - x0) * 0.5f;
        for (int s = 0; s <= 24; s++) {
            float t  = s / 24f;
            float t1 = 1f - t;
            float bx = t1*t1*t1*x0 + 3*t1*t1*t*cx0 + 3*t1*t*t*cx1 + t*t*t*x1;
            float by = t1*t1*t1*y0 + 3*t1*t1*t*y0  + 3*t1*t*t*y1  + t*t*t*y1;
            g.fill((int)bx, (int)by, (int)bx + 2, (int)by + 2, col);
        }
    }

    @Override
    public boolean onMouseClicked(double mx, double my, int btn,
                                  NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        ScriptGraph graph = currentGraph(d);

        // Graph list click
        int listY = oy + 36 + ScriptNodeType.values().length * 18 + 20;
        if (mx >= rx && mx < rx + GRAPH_LIST_W - 2 && d.scriptGraphs != null) {
            int idx = (int)((my - listY) / 12);
            if (idx >= 0 && idx < d.scriptGraphs.size()) {
                graphIndex = idx;
                selectedNode = null;
                return true;
            }
        }

        if (!inCanvas(mx, my)) return false;
        if (graph == null) return false;

        // Hit-test nodes (reverse order so top-drawn is picked first)
        List<ScriptNode> nodes = graph.nodes;
        for (int i = nodes.size() - 1; i >= 0; i--) {
            ScriptNode node = nodes.get(i);
            int nx = (int)(canvasX + panX + node.posX);
            int ny = (int)(canvasY + panY + node.posY);

            // Output port hit
            if (node.type != ScriptNodeType.OUTPUT) {
                int px = nx + NODE_W;
                int py = ny + NODE_H / 2;
                if (Math.abs(mx - px) <= PORT_R && Math.abs(my - py) <= PORT_R) {
                    if (btn == 0) {
                        wireSrc = (wireSrc == node) ? null : node; // toggle wire mode
                        return true;
                    }
                }
            }

            // Input port hit while wiring
            if (wireSrc != null && wireSrc != node && node.type != ScriptNodeType.EVENT) {
                int px = nx;
                int py = ny + NODE_H / 2;
                if (Math.abs(mx - px) <= PORT_R && Math.abs(my - py) <= PORT_R) {
                    if (!wireSrc.outputTo.contains(node.id)) {
                        wireSrc.outputTo.add(node.id);
                    }
                    wireSrc = null;
                    return true;
                }
            }

            // Node body hit
            if (mx >= nx && mx < nx + NODE_W && my >= ny && my < ny + NODE_H) {
                if (btn == 0) {
                    selectedNode = node;
                    nodeParamBox.setValue(node.paramValue);
                    dragNode    = node;
                    dragOffsetX = (float)(mx - (canvasX + panX + node.posX));
                    dragOffsetY = (float)(my - (canvasY + panY + node.posY));
                    return true;
                }
                if (btn == 1) {
                    // Right-click: remove wires from this node
                    wireSrc = null;
                    for (ScriptNode n : graph.nodes) n.outputTo.remove(node.id);
                    node.outputTo.clear();
                    return true;
                }
            }
        }

        // Click on empty canvas: start pan (btn=2 or middle/right in empty area)
        if (btn == 2 || (btn == 0 && wireSrc == null)) {
            panning   = true;
            panStartX = mx;
            panStartY = my;
            panStartPX = panX;
            panStartPY = panY;
            wireSrc   = null;
            selectedNode = null;
        }
        return false;
    }

    @Override
    public boolean onMouseDragged(double mx, double my, int btn, double dx, double dy,
                                  NpcEditorState state, int rx, int oy, int rw) {
        if (dragNode != null) {
            dragNode.posX = (float)(mx - canvasX - panX - dragOffsetX);
            dragNode.posY = (float)(my - canvasY - panY - dragOffsetY);
            return true;
        }
        if (panning) {
            panX = panStartPX + (float)(mx - panStartX);
            panY = panStartPY + (float)(my - panStartY);
            return true;
        }
        return false;
    }

    @Override
    public void onMouseReleased(double mx, double my, int btn,
                                NpcEditorState state, int rx, int oy, int rw) {
        dragNode = null;
        panning  = false;
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        if (inCanvas(mx, my)) {
            panY += (float)(delta * 12);
            return true;
        }
        return false;
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        ScriptGraph cur = currentGraph(d);
        if (cur != null && graphNameBox != null) {
            cur.name = graphNameBox.getValue();
        }
        if (selectedNode != null && nodeParamBox != null) {
            selectedNode.paramValue = nodeParamBox.getValue();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private ScriptGraph currentGraph(NpcEntityData d) {
        if (d.scriptGraphs == null || d.scriptGraphs.isEmpty()) return null;
        if (graphIndex < 0 || graphIndex >= d.scriptGraphs.size()) return null;
        return d.scriptGraphs.get(graphIndex);
    }

    private boolean inCanvas(double mx, double my) {
        return mx >= canvasX && mx < canvasX + canvasW
                && my >= canvasY && my < canvasY + CANVAS_H;
    }

    private void compileAndSave(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        if (d.scriptGraphs == null) return;

        // Replace event chains compiled from graphs (matched by name); append new ones.
        for (ScriptGraph g : d.scriptGraphs) {
            EventChain compiled = ScriptGraphCompiler.compile(g);
            // Replace existing chain with same name, or append
            boolean replaced = false;
            for (int i = 0; i < d.eventChains.size(); i++) {
                if (d.eventChains.get(i).name.equals(compiled.name)) {
                    d.eventChains.set(i, compiled);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) d.eventChains.add(compiled);
        }

        // Apply compiled chains to live entity and send save packet
        state.getEntity().setNpcData(d);
        ModNetwork.CHANNEL.sendToServer(new SaveNpcEntityDataPacket(state.getEntity().getUUID(), d));
    }

    private static int blend(int base, int overlay, float t) {
        int ar = (base >> 24) & 0xFF, br = (overlay >> 24) & 0xFF;
        int ag = (base >> 16) & 0xFF, bg = (overlay >> 16) & 0xFF;
        int ab = (base >>  8) & 0xFF, bb = (overlay >>  8) & 0xFF;
        int ac = (base)       & 0xFF, bc = (overlay)       & 0xFF;
        return ((int)(ar + (br - ar) * t) << 24) |
               ((int)(ag + (bg - ag) * t) << 16) |
               ((int)(ab + (bb - ab) * t) <<  8) |
                (int)(ac + (bc - ac) * t);
    }
}
