package com.mentalfrostbyte.jello.misc;

import java.lang.reflect.InvocationTargetException;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.event.impl.MouseHoverEvent;
import com.mentalfrostbyte.jello.gui.unmapped.Bound;
import com.mentalfrostbyte.jello.managers.GuiManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import team.sdhq.eventBus.EventBus;

public class ModuleKeyPress {
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public static void press(int key) {
      if (Client.getInstance().clientMode != ClientMode.NOADDONS) {
         if (key != -1) {
            for (Bound var5 : Client.getInstance().moduleManager.getMacOSTouchBar().method13733(key)) {
               if (var5 != null && var5.hasTarget()) {
                  switch (Class8614.field38740[var5.getKeybindTypes().ordinal()]) {
                     case 1:
                        var5.getModuleTarget().toggle();
                        break;
                     case 2:
                        try {
                           Screen var6 = var5.getScreenTarget()
                                 .getDeclaredConstructor(Text.class)
                                 .newInstance(new LiteralText(
                                       GuiManager.screenToScreenName.get(var5.getScreenTarget())));
                           if (Client.getInstance().guiManager.method33484(var6)) {
                              mc.displayGuiScreen(var6);
                           }
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                              | NoSuchMethodException | SecurityException | InstantiationException var7) {
                           var7.printStackTrace();
                        }
                  }
               }
            }
         }
      }
   }

   public static void listen(int var0) {
      MouseHoverEvent var3 = new MouseHoverEvent(var0);
      EventBus.call(var3);
   }
}
