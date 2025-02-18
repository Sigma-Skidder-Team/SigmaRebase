package com.mentalfrostbyte.jello.util.system;

import com.mentalfrostbyte.Client;
import org.apache.commons.io.IOUtils;
import totalcross.json.JSONException2;
import totalcross.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static boolean freshConfig = false;

    public static void save(JSONObject var0, File var1) throws IOException {
        FileOutputStream var4 = new FileOutputStream(var1);
        IOUtils.write(var0.toString(0), var4);
        var4.close();
    }

    public static JSONObject readFile(File var0) throws IOException {
        JSONObject var3 = new JSONObject();
        if (var0.exists()) {
            FileInputStream var4 = new FileInputStream(var0);
            String var5 = IOUtils.toString(var4);
            if (var5 == null) {
                var4.close();
                return new JSONObject();
            }

            if (!var5.isEmpty()) {
                try {
                    var3 = new JSONObject(var5);
                } catch (JSONException2 var7) {
                    if (Client.getInstance().getLogger() != null) {
                        Client.getInstance().getLogger().warn("Error when reading json from config. Continuing, but no preferences will be loaded.");
                    }

                    var7.printStackTrace();
                }
            } else if (Client.getInstance().getLogger() != null) {
                Client.getInstance().getLogger().warn("Empty config file");
            }

            var4.close();
        } else {
            if (Client.getInstance().getLogger() != null) {
                Client.getInstance().getLogger().info("Config does not exist... creating new config file...");
            }

            freshConfig = true;
            var0.createNewFile();
        }

        return var3;
    }

    public static File getFileFromDialog() {
        FileDialog fileDialog = new FileDialog((Frame) null, "Select a .txt file");
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setFile("*.txt");

        fileDialog.setVisible(true);
        fileDialog.setAlwaysOnTop(true);

        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }

        return new File(fileDialog.getDirectory(), fileName);
    }
}