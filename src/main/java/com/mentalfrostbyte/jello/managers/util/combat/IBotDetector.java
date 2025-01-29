package com.mentalfrostbyte.jello.managers.util.combat;

import net.minecraft.entity.Entity;

public interface IBotDetector {
    boolean isBot(Entity entity);

    boolean isNotBot(Entity entity);
}
