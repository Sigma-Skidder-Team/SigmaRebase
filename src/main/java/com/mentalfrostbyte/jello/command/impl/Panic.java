package com.mentalfrostbyte.jello.command.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.command.Command;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandArguments;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandExecutor;
import com.mentalfrostbyte.jello.managers.util.command.CommandException;
import com.mentalfrostbyte.jello.managers.util.profile.Configuration;
import com.mentalfrostbyte.jello.managers.ProfileManager;
import totalcross.json.JSONObject;

public class Panic extends Command {
   public Panic() {
      super("panic", "Disable all modules");
   }

   @Override
   public void run(String var1, ChatCommandArguments[] var2, ChatCommandExecutor var3) throws CommandException {
      if (var2.length > 0) {
         throw new CommandException("Too many arguments");
      } else {
         ProfileManager profileManager = Client.getInstance().moduleManager.getConfigurationManager();
         if (profileManager.getConfigByCaseInsensitiveName("Panic")) {
            int configCount = profileManager.getAllConfigs().size();

            for (int var8 = 0; var8 < configCount; var8++) {
               Configuration var9 = profileManager.getAllConfigs().get(var8);
               if (var9.getName.equals("Panic")) {
                  profileManager.checkConfig(var9);
                  var8--;
                  configCount--;
               }
            }
         }

         Configuration panicConfig = new Configuration("Panic", new JSONObject());
         profileManager.saveConfig(panicConfig);
         profileManager.loadConfig(panicConfig);
         var3.send("All modules disabled.");
      }
   }
}
