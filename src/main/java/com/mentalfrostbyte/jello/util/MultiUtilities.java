package com.mentalfrostbyte.jello.util;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import com.mentalfrostbyte.jello.util.player.MovementUtil;

import static com.mentalfrostbyte.jello.module.Module.mc;

public class MultiUtilities {

    public static boolean method17686() {
        return mc.player.moveStrafing != 0.0F || mc.player.moveForward != 0.0F;
    }

    private static boolean field24954 = false;

    public static boolean isAboveBounds(Entity var0, float var1) {
        AxisAlignedBB var4 = new AxisAlignedBB(
                var0.getBoundingBox().minX,
                var0.getBoundingBox().minY - (double) var1,
                var0.getBoundingBox().minZ,
                var0.getBoundingBox().maxX,
                var0.getBoundingBox().maxY,
                var0.getBoundingBox().maxZ);
        Stream<VoxelShape> var5 = mc.world.getCollisionShapes(mc.player, var4);
        return var5.findAny().isPresent();
    }

    public static boolean isCubecraft() {
        return mc.getIntegratedServer() == null && mc.getCurrentServerEntry() != null
                && mc.getCurrentServerEntry().address.toLowerCase().contains("cubecraft.net");
    }
    public static List<PlayerEntity> method17680() {
        ArrayList<PlayerEntity> var2 = new ArrayList<>();
        mc.world.entitiesById.forEach((var1, var2x) -> {
            if (var2x instanceof PlayerEntity) {
                var2.add((PlayerEntity)var2x);
            }
        });
        return var2;
    }
    public static void sendChatMessage(String text) {
        mc.getConnection().sendPacket(new CChatMessagePacket(text));
    }


    public static boolean isHypixel() {
        return !field24954
                && mc.getIntegratedServer() == null
                && mc.getCurrentServerEntry() != null
                && mc.getCurrentServerEntry().address.toLowerCase().contains("hypixel.net");
    }

    public static double setPlayerYMotion(double var0) {
        mc.player.setMotion(mc.player.getMotion().x, var0, mc.player.getMotion().z);
        return var0;
    }
    public static boolean method17729() {
        AxisAlignedBB var2 = mc.player.getBoundingBox().offset(0.0, -1.0, 0.0);
        if (mc.player.getRidingEntity() != null) {
            double var4 = mc.player.getRidingEntity().prevPosX - mc.player.getRidingEntity().getPosX();
            double var6 = mc.player.getRidingEntity().prevPosZ - mc.player.getRidingEntity().getPosZ();
            var2 = mc.player.getRidingEntity().getBoundingBox().expand(Math.abs(var4), 1.0, Math.abs(var6));
        }

        Stream<VoxelShape> var3 = mc.world.getCollisionShapes(mc.player, var2);
        return var3.findAny().isPresent();
    }
    public static int applyAlpha(int color, float alpha) {
        return (int)(alpha * 255.0F) << 24 | color & 16777215;
    }


    public static void block() {
        mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
    }

    public static void unblock() {
        mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM,
                new BlockPos(0, 0, 0), Direction.DOWN));
    }

    public static double method17750() {
        return Math.random() * 1.0E-8;
    }

    public static void method17749(boolean var0) {
        double var3 = mc.player.getPosX();
        double var5 = mc.player.getPosY();
        double var7 = mc.player.getPosZ();
        int var9 = 49 + MovementUtil.getJumpBoost() * 17;

        for (int var10 = 0; var10 < var9; var10++) {
            double var11 = !var0 ? 0.0 : method17750();
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var3 + var11, var5 + 0.06248 + method17750(),
                    var7 + var11, false));
            if (isHypixel()) {
                mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var3 + var11,
                        var5 + 0.05 + method17750(), var7 + var11, false));
            }
        }
    }
}
