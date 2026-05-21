package com.frametrip.dragonlegacyquesttoast.server.combat;

import net.minecraft.world.phys.Vec3;

// [CMB-1]: Formation geometry — calculates per-slot world offsets.
public enum FormationType {

    LINE     ("Шеренга"),
    WEDGE    ("Клин"),
    CIRCLE   ("Круг"),
    SURROUND ("Окружение");

    public final String label;

    FormationType(String label) { this.label = label; }

    /**
     * Returns Vec3 offsets (relative to leader) for each formation slot.
     * Slot 0 is always the leader position (0,0,0).
     * facing — unit vector from leader toward target.
     */
    public Vec3[] calculateOffsets(Vec3 facing, int memberCount) {
        Vec3[] offsets = new Vec3[memberCount];
        offsets[0] = Vec3.ZERO;
        // Perpendicular vector (right side, horizontal)
        Vec3 right = new Vec3(-facing.z, 0, facing.x).normalize();

        switch (this) {
            case LINE -> {
                // Members spread sideways, 2 blocks apart.
                for (int i = 1; i < memberCount; i++) {
                    double side = ((i + 1) / 2) * 2.0 * (i % 2 == 0 ? -1 : 1);
                    offsets[i] = right.scale(side);
                }
            }
            case WEDGE -> {
                // V-shape: odd to right, even to left, each 1.5 blocks behind and 2 blocks apart.
                for (int i = 1; i < memberCount; i++) {
                    int row = (i + 1) / 2;
                    double side = row * 2.0 * (i % 2 == 0 ? -1 : 1);
                    offsets[i] = right.scale(side).add(facing.scale(-row * 1.5));
                }
            }
            case CIRCLE -> {
                // Equidistant around the target position (place 3 blocks behind leader).
                double radius = 3.0;
                for (int i = 1; i < memberCount; i++) {
                    double angle = (2 * Math.PI * i) / memberCount;
                    offsets[i] = new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                }
            }
            case SURROUND -> {
                // Fan behind the target — semicircle behind the leader.
                double radius = 4.0;
                for (int i = 1; i < memberCount; i++) {
                    double angle = Math.PI + (Math.PI * (i - 1.0) / Math.max(1, memberCount - 2)) - Math.PI / 2.0;
                    offsets[i] = new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                }
            }
        }
        return offsets;
    }

    public static FormationType fromName(String name) {
        for (FormationType t : values()) if (t.name().equalsIgnoreCase(name)) return t;
        return LINE;
    }
}
