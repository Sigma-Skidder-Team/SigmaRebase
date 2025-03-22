package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import net.minecraft.network.IPacket;
import org.apache.commons.lang3.reflect.FieldUtils;
import team.sdhq.eventBus.annotations.EventTarget;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

public class PacketDumper extends Module {
    public FileWriter packetWriter;

    public PacketDumper() {
        super(ModuleCategory.MISC, "Packet dumper", "Dumps packets sent to and fro from the client and server");

        try {
            File packetLog = new File(Client.getInstance().file + "/latest_packets.txt");
            if (!packetLog.exists()) {
                packetLog.createNewFile();
            }

            this.packetWriter = new FileWriter(packetLog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractFieldValue(Field field, Object packetInstance) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        if (field.getType() == int.class) {
            return Integer.toString(field.getInt(packetInstance));
        } else if (field.getType() == boolean.class) {
            return Boolean.toString(field.getBoolean(packetInstance));
        } else if (field.getType() == float.class) {
            return Float.toString(field.getFloat(packetInstance));
        } else if (field.getType() == double.class) {
            return Double.toString(field.getDouble(packetInstance));
        } else if (field.getType() == long.class) {
            return Long.toString(field.getLong(packetInstance));
        } else if (field.getType() == char.class) {
            return Character.toString(field.getChar(packetInstance));
        } else if (field.getType() == byte.class) {
            return Byte.toString(field.getByte(packetInstance));
        } else if (field.getType() == short.class) {
            return Short.toString(field.getShort(packetInstance));
        } else {
            return field.get(packetInstance) != null ? field.get(packetInstance).toString() : "null";
        }
    }

    private void logPacket(IPacket packet, boolean isSent) {
        try {
            packetWriter.write((isSent ? "-->" : "<--") + "\t" + packet.getClass().getSimpleName() + "\n");

            for (Field field : FieldUtils.getAllFields(packet.getClass())) {
                try {
                    packetWriter.write("\t\t" + field.getName() + "=" + extractFieldValue(field, packet) + "\n");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventTarget
    public void onPacketSend(EventSendPacket event) {
        if (this.isEnabled()) {
            logPacket(event.packet, true);
        }
    }

    @EventTarget
    public void onPacketReceive(EventReceivePacket event) {
        if (this.isEnabled()) {
            logPacket(event.packet, false);
        }
    }

    /*
    @EventTarget
    public void onShutdown(EventWritter event) {
        try {
            packetWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
     */
}
