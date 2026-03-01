package com.mentalfrostbyte.jello.gui.impl.jello.mainmenu.changelog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mentalfrostbyte.jello.gui.impl.jello.mainmenu.ChangelogScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Change;
import net.minecraft.util.Util;

public class ChangelogParser implements Runnable {
   public final JsonArray changesArray;
   public final ChangelogScreen changelogScreen;

   public ChangelogParser(ChangelogScreen changelogScreen, JsonArray changesArray) {
      this.changelogScreen = changelogScreen;
      this.changesArray = changesArray;
   }

   @Override
   public void run() {
      int currentY = 75;

      try {
         for (int i = 0; i < this.changesArray.size(); i++) {
            JsonObject object = this.changesArray.get(i).getAsJsonObject();
            Change change;
            if (object.has("url")) {
               Util.getOSType().openLink(object.get("url").getAsString());
            }

            this.changelogScreen.scrollPanel.getButtonList().showAlert(change = new Change(this.changelogScreen.scrollPanel, "changelog" + i, object));
            change.setYA(currentY);
            currentY += change.getHeightA();
         }
      } catch (JsonParseException e) {
         throw new RuntimeException(e);
      }
   }
}
