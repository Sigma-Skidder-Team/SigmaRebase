package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.Module;
import net.minecraft.client.gui.screen.Screen;
import totalcross.json.JSONException;
import totalcross.json.JSONObject;

public class Bound {
   private int key = -1;
   private Object target;

   public Bound(JSONObject var1) {
      this.loadFromJSON(var1);
   }

   public Bound(int key, Module target) {
      this.key = key;
      this.target = target;
   }

   public Bound(int key, Class<? extends Screen> target) {
      this.key = key;
      this.target = target;
   }

   public void loadFromJSON(JSONObject from) {
      if (from.has("target")) {
         try {
            if (from.has("key")) {
               this.key = from.getInt("key");
            }

            if (from.has("type")) {
               String var4 = from.getString("type");
               switch (var4) {
                  case "mod":
                     for (Module var7 : Client.getInstance().moduleManager.getModuleMap().values()) {
                        if (from.getString("target").equals(var7.getName())) {
                           this.target = var7;
                        }
                     }
                  case "screen":
                     Class var8 = Client.getInstance().guiManager.method33477(from.getString("target"));
                     if (var8 != null) {
                        this.target = var8;
                     }
               }
            }
         } catch (JSONException e) {
            throw new RuntimeException(e);
         }
      }
   }

   public JSONObject getKeybindData() {
      JSONObject obj = new JSONObject();
       switch (KeybindTypesWrapper.values[this.getKeybindTypes().ordinal()]) {
          case 1:
             obj.put("type", "mod");
             obj.put("target", ((Module)this.target).getName());
             break;
          case 2:
             obj.put("type", "screen");
             obj.put("target", Client.getInstance().guiManager.getNameForTarget((Class<? extends Screen>)this.target));
       }

       obj.put("key", this.key);
       return obj;
   }

   public boolean hasTarget() {
      return this.target != null;
   }

   public int getKeybind() {
      return this.key;
   }

   public void setKey(int var1) {
      this.key = var1;
   }

   public KeybindTypes getKeybindTypes() {
      return !(this.target instanceof Module) ? KeybindTypes.SCREEN : KeybindTypes.MODULE;
   }

   public Object getTarget() {
      return this.target;
   }

   public Class<? extends Screen> getScreenTarget() {
      return (Class<? extends Screen>)this.target;
   }

   public Module getModuleTarget() {
      return (Module)this.target;
   }

   @Override
   public boolean equals(Object to) {
      if (to != this) {
         if (to instanceof Bound) {
            Bound other = (Bound)to;
            return this.getTarget().equals(other.getTarget());
         } else {
            return false;
         }
      } else {
         return true;
      }
   }
}
