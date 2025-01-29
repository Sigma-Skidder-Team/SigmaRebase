package com.mentalfrostbyte.jello.util;

import com.mentalfrostbyte.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

// i couldn't think of better name..
public class NetworkUtil {

    // from MultiUtilities.method17705()
    public static int getPlayerResponseTime() {
        for (NetworkPlayerInfo networkPlayer : MinecraftClient.getInstance().getConnection().getPlayerInfoMap()) {
            if (networkPlayer.getGameProfile().getId().equals(MinecraftClient.getInstance().player.getUniqueID())
                    && !MinecraftClient.getInstance().isIntegratedServerRunning()) {
                return networkPlayer.getResponseTime();
            }
        }

        return 0;
    }

    public static boolean download(boolean finished) {
        if (!finished) {
            // Determine the OS type
            Util.OS osType = Util.getOSType();
            if (osType == Util.OS.WINDOWS || osType == Util.OS.OSX || osType == Util.OS.LINUX) {
                // Ensure the target directory exists
                File musicDir = new File(Client.getInstance().file + "/music/");
                if (!musicDir.exists() && !musicDir.mkdirs()) {
                    System.out.println("Failed to create music directory.");
                    return false;
                }

                // Determine the file name based on the OS
                String fileName = osType == Util.OS.WINDOWS ? "yt-dlp.exe"
                        : osType == Util.OS.LINUX ? "yt-dlp_linux"
                                : "yt-dlp_macos";

                File targetFile = new File(musicDir, fileName);

                try {
                    // Correct URL construction
                    String urlString = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/" + fileName;
                    System.out.println("Downloading yt-dlp from: " + urlString);

                    // Download the file
                    try (BufferedInputStream in = new BufferedInputStream(new URL(urlString).openStream());
                            FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }
                    }

                    // Mark the process as finished
                    finished = true;
                    System.out.println("Finished downloading yt-dlp to " + targetFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Error downloading yt-dlp: " + e.getMessage());
                    finished = false;
                }
            } else {
                System.out.println("Failed to extract yt-dlp, because your OS is unsupported.");
                finished = false;
            }
        }
        return finished;
    }
}
