package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.server.quest.BranchingDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// [QST-2]: Client-side cache of branching dialogs synced from the server.
public class ClientBranchingDialogState {

    private static List<BranchingDialog> dialogs = new ArrayList<>();

    public static void sync(List<BranchingDialog> incoming) {
        dialogs = incoming != null ? new ArrayList<>(incoming) : new ArrayList<>();
    }

    public static List<BranchingDialog> getAll() {
        return Collections.unmodifiableList(dialogs);
    }

    public static BranchingDialog get(String id) {
        for (BranchingDialog d : dialogs) if (d.id.equals(id)) return d;
        return null;
    }
}
