package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.combat.antikb.*;

public class AntiKnockback extends ModuleWithModuleSettings {
    public AntiKnockback() {
        super(
                ModuleCategory.COMBAT,
                "AntiKnockBack",
                "Prevents you from taking knockback",
                new BasicAntiKB(),
                new AACAntiKB(),
                new DelayAntiKB(),
                new SpartanAntiKB(),
                new MinemenAntiKB(),
                new GommeAntiKB(),
                new VulcanAntiKB(),
                new LegitAntiKB()
        );
    }

    @Override
    public String getFormattedName() {
        return Client.getInstance().clientMode != ClientMode.CLASSIC ? super.getFormattedName() : "AntiVelocity";
    }
}
