package com.mentalfrostbyte.jello.misc;

import java.lang.reflect.InvocationTargetException;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.event.impl.MouseHoverEvent;
import com.mentalfrostbyte.jello.gui.unmapped.Class7957;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.misc.unmapped.Class8614;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import team.sdhq.eventBus.EventBus;

public class ModuleKeyPress {
   private static final Minecraft mc = Minecraft.getInstance();

   public static void press(int key) {
      if (Client.getInstance().clientMode != ClientMode.NOADDONS) {
         if (key != -1) {
            for (Class7957 var5 : Client.getInstance().moduleManager.getMacOSTouchBar().method13733(key)) {
               if (var5 != null && var5.method27052()) {
                  switch (Class8614.field38740[var5.method27055().ordinal()]) {
                     case 1:
                        var5.method27058().toggle();
                        break;
                     case 2:
                        try {
                           Screen var6 = var5.method27057()
                              .getDeclaredConstructor(ITextComponent.class)
                              .newInstance(new StringTextComponent(GuiManager.screenToScreenName.get(var5.method27057())));
                           if (Client.getInstance().guiManager.method33484(var6)) {
                              mc.displayGuiScreen(var6);
                           }
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var7) {
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
