package net.minecraft.client.renderer.entity.layers;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderCapeLayer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.Config;
import team.sdhq.eventBus.EventBus;

public class CapeLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
    public CapeLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerModelIn)
    {
        super(playerModelIn);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isWearing(PlayerModelPart.CAPE) && entitylivingbaseIn.getLocationCape() != null)
        {
            ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.CHEST);

            if (itemstack.getItem() != Items.ELYTRA)
            {
                matrixStackIn.push();
                matrixStackIn.translate(0.0D, 0.0D, 0.125D);
                double d0 = MathHelper.lerp((double)partialTicks, entitylivingbaseIn.prevChasingPosX, entitylivingbaseIn.chasingPosX) - MathHelper.lerp((double)partialTicks, entitylivingbaseIn.prevPosX, entitylivingbaseIn.getPosX());
                double d1 = MathHelper.lerp((double)partialTicks, entitylivingbaseIn.prevChasingPosY, entitylivingbaseIn.chasingPosY) - MathHelper.lerp((double)partialTicks, entitylivingbaseIn.prevPosY, entitylivingbaseIn.getPosY());
                double d2 = MathHelper.lerp((double)partialTicks, entitylivingbaseIn.prevChasingPosZ, entitylivingbaseIn.chasingPosZ) - MathHelper.lerp((double)partialTicks, entitylivingbaseIn.prevPosZ, entitylivingbaseIn.getPosZ());
                float f = entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset);
                double d3 = (double)MathHelper.sin(f * ((float)Math.PI / 180F));
                double d4 = (double)(-MathHelper.cos(f * ((float)Math.PI / 180F)));

                EventRenderCapeLayer eventRenderCapeLayer = new EventRenderCapeLayer(100.0F, 20.0F);
                EventBus.call(eventRenderCapeLayer);

                float f1 = (float)d1 * 10.0F;
                f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                float f2 = (float)(d0 * d3 + d2 * d4) * eventRenderCapeLayer.factor1;
                f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
                float f3 = (float)(d0 * d4 - d2 * d3) * eventRenderCapeLayer.factor1;
                f3 = MathHelper.clamp(f3, -(eventRenderCapeLayer.factor2), eventRenderCapeLayer.factor2);

                if (f2 < 0.0F)
                {
                    f2 = 0.0F;
                }

                if (f2 > 165.0F)
                {
                    f2 = 165.0F;
                }

                if (f1 < -5.0F)
                {
                    f1 = -5.0F;
                }

                float f4 = MathHelper.lerp(partialTicks, entitylivingbaseIn.prevCameraYaw, entitylivingbaseIn.cameraYaw);
                f1 = f1 + MathHelper.sin(MathHelper.lerp(partialTicks, entitylivingbaseIn.prevDistanceWalkedModified, entitylivingbaseIn.distanceWalkedModified) * 6.0F) * 32.0F * f4;

                if (entitylivingbaseIn.isCrouching())
                {
                    f1 += 25.0F;
                }

                float f5 = Config.getAverageFrameTimeSec() * 20.0F;
                f5 = Config.limit(f5, 0.02F, 1.0F);
                entitylivingbaseIn.capeRotateX = MathHelper.lerp(f5, entitylivingbaseIn.capeRotateX, 6.0F + f2 / 2.0F + f1);
                entitylivingbaseIn.capeRotateZ = MathHelper.lerp(f5, entitylivingbaseIn.capeRotateZ, f3 / 2.0F);
                entitylivingbaseIn.capeRotateY = MathHelper.lerp(f5, entitylivingbaseIn.capeRotateY, 180.0F - f3 / 2.0F);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(entitylivingbaseIn.capeRotateX));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(entitylivingbaseIn.capeRotateZ));
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entitylivingbaseIn.capeRotateY));
                IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntitySolid(entitylivingbaseIn.getLocationCape()));
                this.getEntityModel().renderCape(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
                matrixStackIn.pop();
            }
        }
    }
}