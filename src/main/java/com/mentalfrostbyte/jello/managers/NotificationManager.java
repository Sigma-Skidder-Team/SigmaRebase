package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.gui.base.QuadraticEasing;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationManager {
    public Minecraft mc = Minecraft.getInstance();
    private final List<Notification> notifications = new ArrayList<>();
    private final int field39922 = 200, field39923 = 340, field39924 = 64, field39925 = 10, field39926 = 10, field39927 = 10;

    public void send(Notification notification) {
        for (Notification var5 : this.notifications) {
            if (var5.equals(notification)) {
                var5.time.setElapsedTime(Math.min(var5.time.getElapsedTime(), this.field39922 + 1));
                var5.desc = notification.desc;
                var5.field43610++;
                var5.icon = notification.icon;
                return;
            }
        }

        this.notifications.add(notification);
    }

    public float getAnimation(Notification var1) {
        float var4 = (float) Math.min(var1.time.getElapsedTime(), (long) var1.showTime);
        if (!(var4 < (float) this.field39922 * 1.4F)) {
            return !(var4 > (float) var1.showTime - (float) this.field39922)
                    ? 1.0F
                    : QuadraticEasing.easeInQuad(((float) var1.showTime - var4) / (float) this.field39922, 0.0F, 1.0F, 1.0F);
        } else {
            return QuadraticEasing.easeOutQuad(var4 / ((float) this.field39922 * 1.4F), 0.0F, 1.0F, 1.0F);
        }
    }

    public float method31994(int var1) {
        float var4 = 0.0F;

        for (int var5 = 0; var5 < var1; var5++) {
            var4 += this.getAnimation(this.notifications.get(var5));
        }

        return var4 / (float) var1;
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) {
        if (!Minecraft.getInstance().gameSettings.hideGUI) {
            for (int var4 = 0; var4 < this.notifications.size(); var4++) {
                Notification notif = this.notifications.get(var4);
                float var6 = this.getAnimation(notif);
                int var7 = Minecraft.getInstance().getMainWindow().getWidth() - this.field39926 - (int) ((float) this.field39923 * var6 * var6);
                int var8 = this.mc.getMainWindow().getHeight()
                        - this.field39924
                        - this.field39925
                        - var4 * (int) ((float) this.field39924 * this.method31994(var4) + (float) this.field39927 * this.method31994(var4));
                float var9 = Math.min(1.0F, var6);
                int var10 = new Color(0.14F, 0.14F, 0.14F, var9 * 0.93F).getRGB();
                int var11 = new Color(0.0F, 0.0F, 0.0F, Math.min(var6 * 0.075F, 1.0F)).getRGB();
                int var12 = new Color(1.0F, 1.0F, 1.0F, var9).getRGB();
                RenderUtil.drawRoundedRect((float) var7, (float) var8, (float) this.field39923, (float) this.field39924, 10.0F, var9);
                RenderUtil.drawRoundedRect((float) var7, (float) var8, (float) (var7 + this.field39923), (float) (var8 + this.field39924), var10);
                RenderUtil.drawRoundedRect((float) var7, (float) var8, (float) (var7 + this.field39923), (float) (var8 + 1), var11);
                RenderUtil.drawRoundedRect((float) var7, (float) (var8 + this.field39924 - 1), (float) (var7 + this.field39923), (float) (var8 + this.field39924), var11);
                RenderUtil.drawRoundedRect((float) var7, (float) (var8 + 1), (float) (var7 + 1), (float) (var8 + this.field39924 - 1), var11);
                RenderUtil.drawRoundedRect(
                        (float) (var7 + this.field39923 - 1), (float) (var8 + 1), (float) (var7 + this.field39923), (float) (var8 + this.field39924 - 1), var11
                );
                RenderUtil.drawPortalBackground(var7, var8, var7 + this.field39923 - this.field39927, var8 + this.field39924);
                RenderUtil.drawString(
                        ResourceRegistry.JelloLightFont20, (float) (var7 + this.field39924 + this.field39927 - 2), (float) (var8 + this.field39927), notif.title, var12
                );
                RenderUtil.drawString(
                        ResourceRegistry.JelloLightFont14,
                        (float) (var7 + this.field39924 + this.field39927 - 2),
                        (float) (var8 + this.field39927 + ResourceRegistry.JelloLightFont20.getHeight(notif.title)),
                        notif.desc,
                        var12
                );
                RenderUtil.endScissor();
                RenderUtil.drawImage(
                        (float) (var7 + this.field39927 / 2),
                        (float) (var8 + this.field39927 / 2),
                        (float) (this.field39924 - this.field39927),
                        (float) (this.field39924 - this.field39927),
                        notif.icon
                );
            }
        }
    }

    @EventTarget
    public void onTick(EventPlayerTick var1) {
        Iterator var4 = this.notifications.iterator();

        while (var4.hasNext()) {
            Notification var5 = (Notification) var4.next();
            if (var5.time.getElapsedTime() > (long) var5.showTime) {
                var4.remove();
            }
        }
    }

    private void method31998() {
        for (int var3 = 0; var3 < this.notifications.size(); var3++) {
            Notification var4 = this.notifications.get(var3);
            float var5 = this.getAnimation(var4);
            int var6 = Minecraft.getInstance().getMainWindow().getWidth() - this.field39926 - (int) ((float) this.field39923 * var5 * var5);
            int var7 = this.mc.getMainWindow().getHeight()
                    - this.field39924
                    - this.field39925
                    - var3 * (int) ((float) this.field39924 * this.method31994(var3) + (float) this.field39927 * this.method31994(var3));

            for (int var8 = 0; var8 < 3; var8++) {
                var4.field43608[var8] = RenderUtil.getColorFromScreen(var6 + this.field39923 / 3 * var8, var7, var4.field43608[var8]);
                var4.field43609[var8] = RenderUtil.getColorFromScreen(var6 + this.field39923 / 3 * var8, var7 + this.field39924, var4.field43609[var8]);
            }
        }
    }

    public boolean isRenderingNotification() {
        for (Notification notification : this.notifications) {
            if (this.getAnimation(notification) > 0) {
                return true;
            }
        }
        return false;
    }

    public void init() {
        EventBus.register(this);
    }
}
