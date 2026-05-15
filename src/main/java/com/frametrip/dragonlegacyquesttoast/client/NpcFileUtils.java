package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import net.minecraft.client.Minecraft;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class NpcFileUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NpcFileUtils.class);

    public static Path getConfigDir() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(DragonLegacyQuestToastMod.MODID);
    }

    public static Path getSkinsDir()      { return getConfigDir().resolve("skins"); }
    public static Path getLayersDir()     { return getConfigDir().resolve("layers"); }
    public static Path getImportTexDir()  { return getConfigDir().resolve("import/textures"); }
    public static Path getImportAnimDir() { return getConfigDir().resolve("import/animations"); }
    public static Path getImportGeoDir()  { return getConfigDir().resolve("import/models"); }
    public static Path getExportAnimDir() { return getConfigDir().resolve("export/animations"); }

    public static void ensureAllDirsExist() {
        Stream.of(getSkinsDir(), getLayersDir(), getImportTexDir(), getImportAnimDir(),
                  getImportGeoDir(), getExportAnimDir())
              .forEach(p -> {
                  try { Files.createDirectories(p); }
                  catch (IOException e) { LOG.error("Failed to create dir {}", p, e); }
              });
    }

    public static void openInExplorer(Path folder) {
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            LOG.error("Failed to create folder {}", folder, e);
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"explorer.exe", folder.toString()});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", folder.toString()});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", folder.toString()});
            }
        } catch (IOException e) {
            LOG.error("Failed to open folder in explorer: {}", folder, e);
        }
    }
}
