package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.render.Resources;

import java.io.InputStream;
import java.util.*;

import jaco.mp3.player.MP3Player;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class SoundManager {
    public static final Map<String, MP3Player> SOUNDS = new HashMap<>();
    private static final String fileType = ".mp3";
    private static final List<String> VALID_SOUNDS = new ArrayList<>(
            Arrays.asList("activate", "deactivate", "click", "error", "pop", "connect", "switch", "clicksound")
    );

    public void play(String url) {
        if (!VALID_SOUNDS.contains(url)) {
            Client.getInstance().getLogger().warn("Invalid audio file attempted to be played: " + url);
        } else {
            try {
                InputStream audioStream = Resources.readInputStream("com/mentalfrostbyte/gui/resources/audio/" + url + fileType);

                AdvancedPlayer player = new AdvancedPlayer(audioStream);
                new Thread(() -> {
                    try {
                        player.play();
                    } catch (JavaLayerException e) {
                        Client.getInstance().getLogger().error("Error playing audio file: " + url + " : " + e.getMessage());
                    }
                }).start();

                MP3Player sound = new MP3Player(audioStream);
                SOUNDS.put(url, sound);
            } catch (JavaLayerException e) {
                Client.getInstance().getLogger().error("Unsupported audio file: " + url + " : " + e.getMessage());
            }
        }
    }

    public void init() {
    }
}
