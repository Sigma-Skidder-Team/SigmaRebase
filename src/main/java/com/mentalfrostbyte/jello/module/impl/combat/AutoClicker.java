package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.player.action.EventInputOptions;
import com.mentalfrostbyte.jello.event.impl.player.action.EventPlace;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.RayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

public class AutoClicker extends Module {
    public static int del, cpsdel;
    TimerUtil timer = new TimerUtil();
    boolean nigga;

    public AutoClicker() {
        super(ModuleCategory.COMBAT, "AutoClicker", "Longpress your attack keybind to hit entities automaticly");
        this.registerSetting(new NumberSetting<Integer>("Base CPS", "Base click per seconds.", 1, Integer.class, 1, 20, 1));
        this.registerSetting(new NumberSetting<Integer>("Min CPS", "Minimum click per seconds randomization.", 1, Integer.class, 1, 20, 1));
        this.registerSetting(new NumberSetting<Integer>("Max CPS", "Maximum click per seconds randomization.", 1, Integer.class, 1, 20, 1));
        this.registerSetting(new BooleanSetting("AutoBlock", "Automatically blocks for you.", false));
        this.registerSetting(new BooleanSetting("Hover Check", "Blocks only if you are hovering the target.", false));
        this.registerSetting(new NumberSetting<Integer>("Auto Block Ticks", "Autoblock frecuency.", 1, Integer.class, 1, 5, 1));
        this.registerSetting(new BooleanSetting("1.9+ Cooldown", "Use attack cooldown (1.9+).", false));

    }

    public static int randomNumber(int max, int min) {
        int ii = -min + (int) (Math.random() * ((max - (-min)) + 1));
        return ii;
    }

    @EventTarget
    @HighestPriority
    public void onRender(EventRender3D var1) {
        if(PlayerController.isHittingBlock || PlayerController.curBlockDamageMP != 0){

            nigga = false;
        }else{
            nigga = true;
        }
    }

    @EventTarget
    @HighestPriority
    public void meth2od10(EventInputOptions var1) {
        if (!mc.gameSettings.keyBindAttack.pressed) {
            return;
        }
        if(mc.player.getHeldItemMainhand() != null &&
                mc.player.getHeldItemMainhand().getItem() instanceof SwordItem){
            if(getBooleanValueFromSettingName("AutoBlock")){

                if(getBooleanValueFromSettingName("Hover Check")){
                    if(mc.objectMouseOver != null){
                        if(mc.objectMouseOver.getType() != RayTraceResult.Type.ENTITY){
                            return;
                        }
                    }
                }

                if(mc.player.ticksExisted % getNumberValueBySettingName("Auto Block Ticks") == 0){
                    var1.setUseItem(true);
                }
            }
        }
    }

    @EventTarget
    @HighestPriority
    public void onPlaceEvent(EventPlace var1) {
        if (mc.currentScreen == null && mc.player.isAlive()) {

            if (!mc.gameSettings.keyBindAttack.pressed) {
                timer.reset();
                return;
            }

            int cps = (int) getNumberValueBySettingName("Base CPS");
            int minran = (int) getNumberValueBySettingName("Min CPS");
            int maxran = (int) getNumberValueBySettingName("Max CPS");

            float var7 = !((double) mc.player.getCooldownPeriod() < 1.26) && this.getBooleanValueFromSettingName("1.9+ Cooldown") ? mc.player.getCooledAttackStrength(0.0F) : 1.0F;

            int rand = randomNumber(minran, maxran);
            cpsdel = cps+rand <= 0? 1:cps+rand;

            del = 1000/(cpsdel);

            if(!(var7 >= 1.0F)){
                return;
            }

            if ((timer.getElapsedTime() > (del)) && nigga) {
                mc.clickMouse();
                timer.reset();
            }
        }
    }


}