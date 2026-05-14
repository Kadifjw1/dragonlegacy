package com.frametrip.dragonlegacyquesttoast.util;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NpcFileUtils {

    private static final Logger LOGGER = LogManager.getLogger(DragonLegacyQuestToastMod.MODID);
    private static final String MOD_ID = DragonLegacyQuestToastMod.MODID;

    private NpcFileUtils() {}

    // ── Directory helpers ─────────────────────────────────────────────────────

    public static Path modConfigDir() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(MOD_ID);
    }

    public static Path skinsDir()       { return modConfigDir().resolve("skins"); }
    public static Path layersDir()      { return modConfigDir().resolve("layers"); }
    public static Path importTexDir()   { return modConfigDir().resolve("import").resolve("textures"); }
    public static Path importAnimDir()  { return modConfigDir().resolve("import").resolve("animations"); }
    public static Path exportAnimDir()  { return modConfigDir().resolve("export").resolve("animations"); }

    /** Creates all mod directories on first client launch. */
    public static void ensureAllDirsExist() {
        for (Path dir : new Path[]{skinsDir(), layersDir(),
                importTexDir(), importAnimDir(), exportAnimDir()}) {
            try { Files.createDirectories(dir); } catch (IOException ignored) {}
        }
    }

    // ── Cross-platform folder opener ──────────────────────────────────────────

    /**
     * Creates {@code folder} if absent then opens it in the OS file manager.
     * Works on Windows (explorer.exe), macOS (open), and Linux (xdg-open).
     */
    public static void openInExplorer(Path folder) {
        try {
            Files.createDirectories(folder);
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer.exe \"" + folder + "\"");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", folder.toString()});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", folder.toString()});
            }
        } catch (IOException e) {
            LOGGER.error("Cannot open folder: {}", folder, e);
        }
    }
}
