package net.minecraft.util;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.impl.movement.NoSlow;
import net.minecraft.client.GameSettings;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void tickMovement(boolean p_225607_1_)
    {
        this.forwardKeyDown = this.gameSettings.keyBindForward.isKeyDown();
        this.backKeyDown = this.gameSettings.keyBindBack.isKeyDown();
        this.leftKeyDown = this.gameSettings.keyBindLeft.isKeyDown();
        this.rightKeyDown = this.gameSettings.keyBindRight.isKeyDown();
        this.moveForward = this.forwardKeyDown == this.backKeyDown ? 0.0F : (this.forwardKeyDown ? 1.0F : -1.0F);
        this.moveStrafe = this.leftKeyDown == this.rightKeyDown ? 0.0F : (this.leftKeyDown ? 1.0F : -1.0F);
        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneaking = this.gameSettings.keyBindSneak.isKeyDown();

        if (p_225607_1_)
        {
            Module noSlow = Client.getInstance().moduleManager.getModuleByClass(NoSlow.class);
            boolean shouldCancelSneakSlowdown = noSlow.isEnabled2() && this.sneaking && noSlow.getBooleanValueFromSettingName("Sneak");
            if (shouldCancelSneakSlowdown) return;

            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }
}
