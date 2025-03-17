package com.mentalfrostbyte.jello.gui.impl.jello.mainmenu.changelog;

import com.google.gson.JsonParseException;
import com.mentalfrostbyte.jello.gui.impl.jello.mainmenu.ChangelogScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Change;
import net.minecraft.util.Util;

public class Class576 implements Runnable {
   public final JSONArray field2812;
   public final ChangelogScreen field2813;

   public Class576(ChangelogScreen var1, JSONArray var2) {
      this.field2813 = var1;
      this.field2812 = var2;
   }

   @Override
   public void run() {
      int var3 = 75;

      try {
         for (int var4 = 0; var4 < this.field2812.length(); var4++) {
            JSONObject var5 = this.field2812.getJSONObject(var4);
            Change var6;
            if (var5.has("url")) {
               Util.getOSType().openLink(var5.getString("url"));
            }

            this.field2813.scrollPanel.getButton().showAlert(var6 = new Change(this.field2813.scrollPanel, "changelog" + var4, var5));
            var6.setYA(var3);
            var3 += var6.getHeightA();
         }
      } catch (JsonParseException e) {
         throw new RuntimeException(e);
      }
   }
}
