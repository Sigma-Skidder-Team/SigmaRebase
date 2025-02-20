package com.mentalfrostbyte.jello.module.impl.world;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.world.BoundingBox;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import net.minecraft.util.math.ChunkPos;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import net.minecraft.network.play.server.SChunkDataPacket;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.Iterator;

public class NewChunks extends Module {
    private final ArrayList<ChunkPos> newChunks = new ArrayList<>();
    private final ArrayList<ChunkPos> updatedChunks = new ArrayList<>();

    public NewChunks() {
        super(ModuleCategory.WORLD, "NewChunks", "Detects new chunks on non vanilla servers");
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SChunkDataPacket) {
                SChunkDataPacket packet = (SChunkDataPacket) event.packet;
                ChunkPos chunkPos = new ChunkPos(packet.getChunkX(), packet.getChunkZ());
                if (!packet.isFullChunk()) {
                    this.updatedChunks.add(chunkPos);
                }
            }
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (this.isEnabled()) {
            for (Iterator<ChunkPos> iterator = this.updatedChunks.iterator(); iterator.hasNext(); iterator.remove()) {
                ChunkPos chunkPos = iterator.next();
                if (!this.newChunks.contains(chunkPos)) {
                    this.newChunks.add(chunkPos);
                }
            }

            Iterator<ChunkPos> chunkIterator = this.newChunks.iterator();
            while (chunkIterator.hasNext()) {
                ChunkPos chunkPos = chunkIterator.next();
                if (chunkPos != null) {
                    double x = chunkPos.x - mc.gameRenderer.getActiveRenderInfo().getPos().getX();
                    double z = chunkPos.z - mc.gameRenderer.getActiveRenderInfo().getPos().getZ();
                    double y = -mc.gameRenderer.getActiveRenderInfo().getPos().getY();
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    BoundingBox box = new BoundingBox(x, y, z, x + 16.0, y + 16.0, z + 16.0);
                    RenderUtil.render3DColoredBox(box,
                            RenderUtil.applyAlpha(ClientColors.PALE_ORANGE.getColor(), 0.1F));
                    RenderUtil.renderWireframeBox(box,
                            RenderUtil.applyAlpha(ClientColors.PALE_ORANGE.getColor(), 0.1F));
                    GL11.glColor3f(1.0F, 1.0F, 1.0F);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);

                    int dx = mc.player.chunkCoordX - chunkPos.x;
                    int dz = mc.player.chunkCoordZ - chunkPos.z;
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    if (distance > 30.0) {
                        chunkIterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.updatedChunks.clear();
        this.newChunks.clear();
    }
}