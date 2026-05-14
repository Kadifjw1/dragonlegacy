package com.frametrip.dragonlegacyquesttoast.util;

import com.frametrip.dragonlegacyquesttoast.client.NpcLayeredSkinManager;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles importing PNG textures from the import/textures staging directory
 * into either the skins or layers subdirectories.
 */
public final class NpcTextureImporter {

    public static final String TARGET_SKIN = "skin";

    private NpcTextureImporter() {}

    /** Lists filenames (without .png) found in the import/textures directory. */
    public static List<String> scanImportDir() {
        Path dir = NpcFileUtils.importTexDir();
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

    /**
     * Copies a file from import/textures to skins/ (when target == TARGET_SKIN)
     * or to layers/{category}/. Refreshes the corresponding manager.
     *
     * @param filename base filename without .png
     * @param target   TARGET_SKIN or a layer category id (e.g. "hair")
     * @return true on success
     */
    public static boolean importTexture(String filename, String target) {
        Path src = NpcFileUtils.importTexDir().resolve(filename + ".png");
        if (!Files.exists(src)) return false;

        Path dest;
        if (TARGET_SKIN.equals(target)) {
            dest = NpcFileUtils.skinsDir().resolve(filename + ".png");
        } else {
            dest = NpcFileUtils.layersDir().resolve(target).resolve(filename + ".png");
        }

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

    /** Opens the import/textures staging folder in the OS file manager. */
    public static void openImportFolder() {
        NpcFileUtils.openInExplorer(NpcFileUtils.importTexDir());
    }
}
