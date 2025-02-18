package com.mentalfrostbyte.jello.util.client.invmanager;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;

public class InvManagerUtil {

    public static ItemStack regularClick(int var0, int var1, int var2, ClickType var3, PlayerEntity var4) {
        return fixedClick(var0, var1, var2, var3, var4, false);
    }

    public static ItemStack fixedClick(int var0, int var1, int var2, ClickType var3, PlayerEntity var4, boolean var5) {
        ItemStack var8 = null;
        if (var1 >= 0) {
            var8 = var4.openContainer.getSlot(var1).getStack().copy();
        }

        short var9 = var4.openContainer.getNextTransactionID(Minecraft.getInstance().player.inventory);
        ItemStack var10 = var4.openContainer.slotClick(var1, var2, var3, var4);
        if (var8 == null || ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_12) && !var5 || var3 == ClickType.SWAP) {
            var8 = var10;
        }

        Minecraft.getInstance().getConnection().sendPacket(new CClickWindowPacket(var0, var1, var2, var3, var8, var9));
        return var10;
    }
}
