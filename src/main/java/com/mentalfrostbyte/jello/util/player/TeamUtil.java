package com.mentalfrostbyte.jello.util.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;

// https://github.com/Sigma-Skidder-Team/SigmaRemap/blob/main/src/java/mapped/Class8781.java
public class TeamUtil {

    public static boolean method31662(PlayerEntity var0) {
        assert MinecraftClient.getInstance().player != null;
        return method31663(MinecraftClient.getInstance().player) == method31663(var0);
    }

    public static int method31663(PlayerEntity var0) {
        ScorePlayerTeam var3 = (ScorePlayerTeam) var0.getTeam();
        return var3 != null && var3.getColor().getColor() != null ? var3.getColor().getColor() : 16777215;
    }
}
