package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.data.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.highjump.HypixelHighJump;
import com.mentalfrostbyte.jello.module.impl.movement.highjump.MineplexHighJump;
import com.mentalfrostbyte.jello.module.impl.movement.highjump.MinibloxHighJump;
import com.mentalfrostbyte.jello.module.impl.movement.highjump.VanillaHighJump;

public class HighJump extends ModuleWithModuleSettings {
    public HighJump() {
        super(ModuleCategory.MOVEMENT,
                "HighJump",
                "Makes you jump higher",
                new VanillaHighJump(),
                new MineplexHighJump(),
                new MinibloxHighJump(),
                new HypixelHighJump()
        );
    }
}
