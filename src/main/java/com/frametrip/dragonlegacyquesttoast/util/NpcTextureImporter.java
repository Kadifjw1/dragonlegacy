package com.frametrip.dragonlegacyquesttoast.util;

import com.frametrip.dragonlegacyquesttoast.client.NpcFileUtils;
import com.frametrip.dragonlegacyquesttoast.client.NpcLayeredSkinManager;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NpcTextureImporter {

    public static final String TARGET_SKIN = "skin";

    private NpcTextureImporter() {}

    public static List<String> scanImportDir() {
        Path dir = NpcFileUtils.getImportTexDir();
        List<String> result = new ArrayList<>();
        if (!Files.isDirectory(dir)) return result;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.png")) {
            for (Path p : ds) {
                String name = p.getFileName().toString();
                result.add(name.substring(0, name.length() - 4));
            }
        } catch (IOException ignored) {}
        Collections.sort(result);
        return result;
    }

    public static boolean importTexture(String filename, String target) {
        Path src = NpcFileUtils.getImportTexDir().resolve(filename + ".png");
        if (!Files.exists(src)) return false;

        Path dest = TARGET_SKIN.equals(target)
                ? NpcFileUtils.getSkinsDir().resolve(filename + ".png")
                : NpcFileUtils.getLayersDir().resolve(target).resolve(filename + ".png");

        try {
            Files.createDirectories(dest.getParent());
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return false;
        }

        if (TARGET_SKIN.equals(target)) {
            NpcSkinManager.refresh();
        } else {
            NpcLayeredSkinManager.refresh();
        }
        return true;
    }

    public static void openImportFolder() {
        NpcFileUtils.openInExplorer(NpcFileUtils.getImportTexDir());
    }
}
