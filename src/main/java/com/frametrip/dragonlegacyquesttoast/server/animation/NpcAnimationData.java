package com.frametrip.dragonlegacyquesttoast.server.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NpcAnimationData {
    public String id             = UUID.randomUUID().toString().substring(0, 8);
    public String name           = "Анимация";
    public float  durationTicks  = 20f;
    public boolean loop          = false;
    public AnimationState stateBinding = AnimationState.CUSTOM;
    public List<AnimationBone> bones   = new ArrayList<>();

    /** Ensure all 6 bones exist. */
    public void ensureBones() {
        for (String boneId : AnimationBone.BONE_IDS) {
            boolean found = bones.stream().anyMatch(b -> b.boneName.equals(boneId));
            if (!found) bones.add(new AnimationBone(boneId));
        }
    }

    public AnimationBone getBone(String boneName) {
        for (AnimationBone b : bones) if (b.boneName.equals(boneName)) return b;
        AnimationBone b = new AnimationBone(boneName);
        bones.add(b);
        return b;
    }

    public NpcAnimationData copy() {
        NpcAnimationData c = new NpcAnimationData();
        c.id            = this.id;
        c.name          = this.name;
        c.durationTicks = this.durationTicks;
        c.loop          = this.loop;
        c.stateBinding  = this.stateBinding;
        for (AnimationBone b : bones) c.bones.add(b.copy());
        return c;
    }

    /** Export to GeckoLib-compatible JSON string. */
    public String toGeckoLibJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"format_version\": \"1.8.0\",\n  \"animations\": {\n");
        sb.append("    \"animation.npc.").append(name.toLowerCase().replace(' ', '_')).append("\": {\n");
        sb.append("      \"loop\": ").append(loop).append(",\n");
        sb.append("      \"animation_length\": ").append(String.format("%.3f", durationTicks / 20f)).append(",\n");
        sb.append("      \"bones\": {\n");
        for (int i = 0; i < bones.size(); i++) {
            AnimationBone bone = bones.get(i);
            sb.append("        \"").append(bone.boneName).append("\": {\n");
            appendKeyframeChannel(sb, "rotation", bone.rotationFrames);
            if (!bone.positionFrames.isEmpty()) {
                sb.append(",\n");
                appendKeyframeChannel(sb, "position", bone.positionFrames);
            }
            sb.append("\n        }");
            if (i < bones.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("      }\n    }\n  }\n}");
        return sb.toString();
    }

    private void appendKeyframeChannel(StringBuilder sb, String channel, List<AnimationKeyframe> frames) {
        sb.append("          \"").append(channel).append("\": {");
        for (int i = 0; i < frames.size(); i++) {
            AnimationKeyframe f = frames.get(i);
            String t = String.format("%.3f", f.tick / 20f);
            sb.append("\n            \"").append(t).append("\": [")
              .append(f.x).append(", ").append(f.y).append(", ").append(f.z).append("]");
            if (i < frames.size() - 1) sb.append(",");
        }
        if (!frames.isEmpty()) sb.append("\n          }");
        else sb.append("}");
    }
}
