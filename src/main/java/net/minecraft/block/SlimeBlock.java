package net.minecraft.block;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.impl.movement.NoSlow;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class SlimeBlock extends BreakableBlock
{
    public SlimeBlock(AbstractBlock.Properties properties)
    {
        super(properties);
    }

    /**
     * Block's chance to react to a living entity falling on it.
     */
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance)
    {
        if (entityIn.isSuppressingBounce())
        {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
        else
        {
            entityIn.onLivingFall(fallDistance, 0.0F);
        }
    }

    /**
     * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
     * on its own
     */
    public void onLanded(IBlockReader worldIn, Entity entityIn)
    {
        if (entityIn.isSuppressingBounce())
        {
            super.onLanded(worldIn, entityIn);
        }
        else
        {
            this.bounceEntity(entityIn);
        }
    }

    private void bounceEntity(Entity entity)
    {
        Vector3d vector3d = entity.getMotion();

        if (vector3d.y < 0.0D)
        {
            double d0 = entity instanceof LivingEntity ? 1.0D : 0.8D;
            entity.setMotion(vector3d.x, -vector3d.y * d0, vector3d.z);
        }
    }

    /**
     * Called when the given entity walks on this Block
     */
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        if (entityIn instanceof ClientPlayerEntity && noSlow().isEnabled2() && noSlow().blocks.currentValue) return;
        double d0 = Math.abs(entityIn.getMotion().y);

        if (d0 < 0.1D && !entityIn.isSteppingCarefully())
        {
            double d1 = 0.4D + d0 * 0.2D;
            entityIn.setMotion(entityIn.getMotion().mul(d1, 1.0D, d1));
        }

        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public float getSlipperiness() {
        // MODIFICATION BEGIN: less slipperiness if NoSlow is enabled, this is the local player, and Blocks is enabled in NoSlow.
        if (noSlow().isEnabled2() && noSlow().blocks.currentValue)
            return 0.6f;
        return super.getSlipperiness();
        // MODIFICATION END
    }

    private NoSlow noSlow() {
        return (NoSlow)Client.getInstance().moduleManager.getModuleByClass(NoSlow.class);
    }
}
