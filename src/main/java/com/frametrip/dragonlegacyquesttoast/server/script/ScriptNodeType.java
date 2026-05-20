package com.frametrip.dragonlegacyquesttoast.server.script;

// [SCR-1]: Five node types for the visual script graph editor.
public enum ScriptNodeType {
    EVENT    ("СОБЫТИЕ",   0xFF993333, 0xFFCC4444),
    CONDITION("УСЛОВИЕ",   0xFF886600, 0xFFCCAA00),
    ACTION   ("ДЕЙСТВИЕ",  0xFF334488, 0xFF4488CC),
    DELAY    ("ЗАДЕРЖКА",  0xFF444444, 0xFF777777),
    OUTPUT   ("КОНЕЦ",     0xFF337744, 0xFF44AA55);

    /** Display label shown on the node header. */
    public final String label;
    /** Darker header bar colour. */
    public final int headerColor;
    /** Lighter body colour. */
    public final int bodyColor;

    ScriptNodeType(String label, int headerColor, int bodyColor) {
        this.label       = label;
        this.headerColor = headerColor;
        this.bodyColor   = bodyColor;
    }
}
