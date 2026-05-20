package com.frametrip.dragonlegacyquesttoast.server.combat;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// [CMB-1]: Tracks live formation membership and issues coordinated target positions.
public class FormationController {

    // formationId -> ordered list of member UUIDs (index = slot)
    private static final Map<String, List<UUID>> formations = new ConcurrentHashMap<>();

    public static void join(String formationId, UUID npcUuid) {
        formations.computeIfAbsent(formationId, k -> new ArrayList<>()).add(npcUuid);
    }

    public static void leave(String formationId, UUID npcUuid) {
        List<UUID> members = formations.get(formationId);
        if (members != null) {
            members.remove(npcUuid);
            if (members.isEmpty()) formations.remove(formationId);
        }
    }

    public static List<UUID> getMembers(String formationId) {
        List<UUID> members = formations.get(formationId);
        return members != null ? Collections.unmodifiableList(members) : Collections.emptyList();
    }

    /**
     * Leader (slot 0) calls this when it acquires a target.
     * All members receive calculated world positions to move toward.
     */
    public static void setFormationTarget(NpcEntity leader, String formationId,
                                          FormationType type, LivingEntity target,
                                          ServerLevel level) {
        List<UUID> memberIds = formations.getOrDefault(formationId, Collections.emptyList());
        if (memberIds.isEmpty()) return;

        Vec3 leaderPos = leader.position();
        Vec3 toTarget = target.position().subtract(leaderPos);
        Vec3 facing = toTarget.lengthSqr() > 0
            ? new Vec3(toTarget.x, 0, toTarget.z).normalize()
            : new Vec3(1, 0, 0);

        Vec3[] offsets = type.calculateOffsets(facing, memberIds.size());

        for (int i = 0; i < memberIds.size(); i++) {
            Entity e = level.getEntity(memberIds.get(i));
            if (!(e instanceof NpcEntity member)) continue;
            Vec3 targetPos = leaderPos.add(offsets[i]);
            member.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0);
            member.setTarget(target);
        }
    }
}
