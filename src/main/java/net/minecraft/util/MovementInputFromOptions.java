package net.minecraft.util;

import net.minecraft.client.GameSettings;
import com.mentalfrostbyte.jello.event.impl.player.action.EventInputOptions;
import team.sdhq.eventBus.EventBus;

public class MovementInputFromOptions extends MovementInput {
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings var1) {
        this.gameSettings = var1;
    }


    public void tickMovement(boolean var1) {
        EventInputOptions eventInputOptions = new EventInputOptions(1,1, this.gameSettings.keyBindSneak.isKeyDown(), this.gameSettings.keyBindJump.isKeyDown());
        EventBus.call(eventInputOptions);
        moveForward = 0.0f;
        moveStrafe = 0.0f;

        this.forwardKeyDown = this.gameSettings.keyBindForward.isKeyDown();
        this.backKeyDown = this.gameSettings.keyBindBack.isKeyDown();

        this.leftKeyDown = this.gameSettings.keyBindLeft.isKeyDown();
        this.rightKeyDown = this.gameSettings.keyBindRight.isKeyDown();


        if (forwardKeyDown) {
            moveForward+= eventInputOptions.getForward();
        }

        if (backKeyDown) {
            moveForward-= eventInputOptions.getForward();
        }

        if (leftKeyDown) {
            moveStrafe+= eventInputOptions.getStrafe();
        }

        if (rightKeyDown) {
            moveStrafe-= eventInputOptions.getStrafe();
        }

        this.jump = eventInputOptions.isJumping();
        this.sneaking = eventInputOptions.isSneaking();

        if (var1) {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3);
            this.moveForward = (float)((double)this.moveForward * 0.3);
        }
    }
}
