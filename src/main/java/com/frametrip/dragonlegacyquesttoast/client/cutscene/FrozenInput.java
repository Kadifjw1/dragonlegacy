package com.frametrip.dragonlegacyquesttoast.client.cutscene;

import net.minecraft.client.player.Input;

// [VFX-3]: Blocks all player movement input during cutscenes.
public class FrozenInput extends Input {
    @Override
    public void tick(boolean isSneaking, float sneakingSpeedBonus) {
        left = right = up = down = false;
        jumping = shiftKeyDown = false;
        forwardImpulse  = 0f;
        leftImpulse     = 0f;
    }
}
