package com.mentalfrostbyte.jello.util.client;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ModuleSettingInitializr implements Runnable {
    public static Thread thisThread = new Thread(new ModuleSettingInitializr());
    public static HashMap<Object, Integer> field8343;

    static {
        thisThread.start();
    }

    @Override
    public void run() {
        field8343 = new HashMap<>();

        while (!Thread.currentThread().isInterrupted()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (Minecraft.getInstance().world != null) {
                boolean var3 = false;
                boolean var4 = false;
                if (Client.getInstance().moduleManager != null) {
                    List<Module> var5 = new ArrayList<>(Client.getInstance().moduleManager.getModuleMap().values());

                    for (Module var7 : Client.getInstance().moduleManager.getModuleMap().values()) {
                        if (var7 instanceof ModuleWithModuleSettings) {
                            var5.addAll(Arrays.asList(((ModuleWithModuleSettings) var7).moduleArray));
                        }
                    }

                    for (Module var10 : var5) {
                        if (var10.getClass().getSuperclass() != Module.class && var10.getClass().getSuperclass() != ModuleWithModuleSettings.class) {
                            var3 = true;
                            if (field8343.containsKey(var10) && field8343.get(var10) != var10.getRandomAssOffset()) {
                                var4 = true;
                            }

                            field8343.put(var10, var10.getRandomAssOffset());
                        }
                    }
                }
            }
        }
    }
}
