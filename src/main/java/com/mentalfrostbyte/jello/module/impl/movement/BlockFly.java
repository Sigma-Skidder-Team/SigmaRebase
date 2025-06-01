package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMoveButton;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.blockfly.BlockFlyHelper;
import com.mentalfrostbyte.jello.module.impl.movement.blockfly.BlockUtils;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.player.rotation.RotationCore;
import com.mentalfrostbyte.jello.util.game.player.rotation.util.RandomUtil;
import com.mentalfrostbyte.jello.util.game.player.rotation.util.RotationUtils;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.mentalfrostbyte.jello.util.system.math.MathHelper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

import java.util.Arrays;
import java.util.List;

public class BlockFly extends Module {
    //Block amount
    private final Animation animation = new Animation(400, 400, Animation.Direction.BACKWARDS);

    //Rotations & cache
    private BlockFlyHelper.BlockCache blockPos = null;
    private float[] rots = new float[]{0, 0};

    //Placing & blocks
    private final List<Block> BLACKLISTED_BLOCKS;
    private int blockAmount = 0, packetBlockSlot, sneakAfterTick = 0, placedBlocks;
    private boolean placeUp;

    //Item switch
    private int slot = -1, itemPrevEnable = -1;

    //KeepY
    private double y = 0.0;

    //Raycasting
    private Vector3d ray = null;

    //Ticks
    private int offGroundTicks, damageTicks;

    public BlockFly() {
        super(ModuleCategory.MOVEMENT, "BlockFly", "Allows you to automatically bridge");
        this.registerSetting(new ModeSetting("Mode", "Rotation strategy for scaffold rotation.", 1, "Grim", "Hypixel", "SideHit", "Clutch", "AAC"));
        this.registerSetting(new BooleanSetting("Static Pitch", "Special mode for Hypixel Mode.", false));
        this.registerSetting(new NumberSetting<>("Pitch Value", "Pitch value for Hypixel Mode.", 70F, 70F, 90F, 0.2F) {
            @Override
            public boolean isHidden() {
                return !getBooleanValueFromSettingName("Static Pitch");
            }
        });

        this.registerSetting(new ModeSetting("Smart Block Finder", "Adjust rotation strategy when no block is found.", 1, "Basic", "Test1", "Test2"));
        this.registerSetting(new ModeSetting("ItemSpoof", "Item spoofing mode.", 2, "None", "Switch", "Spoof", "LiteSpoof"));
        this.registerSetting(new ModeSetting("Tower Mode", "Tower mode", 1, "None", "NCP", "AAC", "Vanilla"));
        this.registerSetting(new ModeSetting("Picking mode", "The way it will move blocks in your inventory.", 0, "Basic", "FakeInv", "OpenInv"));
        this.registerSetting(new BooleanSetting("Tower while moving", "Allows you to tower while moving.", false));
        this.registerSetting(new BooleanSetting("AutoJump", "Keep jumping while bridging.", false));
        this.registerSetting(new BooleanSetting("Verus Yport", "Makes you go fast on Verus.", false));
        this.registerSetting(new BooleanSetting("Raytrace", "Helps the BlockFly become more legit.", false));
        this.registerSetting(new BooleanSetting("SameY", "Keep same height while jumping.", false));
        this.registerSetting(new BooleanSetting("Show Block Amount", "Shows the amount of blocks in your inventory.", true));
        this.registerSetting(new BooleanSetting("NoSwing", "Removes the swing animation.", false));
        this.registerSetting(new BooleanSetting("Sprint", "Sprint even with movefix.", true));
        this.registerSetting(new NumberSetting<Float>("Extend", "Extend value.", 0.0F, 0.0F, 6.0F, 0.1F));
        this.registerSetting(new NumberSetting<Float>("Search", "Max block distance area to place blocks.", 1.0F, 1.0F, 5.0F, 1F));
        this.registerSetting(new NumberSetting<Float>("Rotation Speed", "Max rotation change per tick.", 180.0F, 6.0F, 360, 6F));
        this.registerSetting(new BooleanSetting("Intelligent Block Picker", "Always get the biggest blocks stack.", true));
        this.registerSetting(new BooleanSetting("Eagle", "Doesn't let you fall off edges sneaking.", false));
        this.registerSetting(new NumberSetting<Integer>("Eagle Rate", "Placed blocks before eagle.", 0.0F, 0, 8, 1) {
            @Override
            public boolean isHidden() {
                return !getBooleanValueFromSettingName("Eagle");
            }
        });
        this.registerSetting(new NumberSetting<Integer>("Eagle Ticks", "Eagle tick duration.", 1, 1, 8, 1) {
            @Override
            public boolean isHidden() {
                return !getBooleanValueFromSettingName("Eagle");
            }
        });
        this.registerSetting(new NumberSetting<Float>("Eagle Gap", "Distance from the block edges to sneak.", 0.0F, 0, 1, 0.05f) {
            @Override
            public boolean isHidden() {
                return !getBooleanValueFromSettingName("Eagle");
            }
        });
        this.registerSetting(new BooleanSetting("Eagle Gap Rand", "Add Randomization to distance from the block edges", true) {
            @Override
            public boolean isHidden() {
                return !getBooleanValueFromSettingName("Eagle");
            }
        });
        this.registerSetting(new BooleanSetting("Clutch", "Place a block under when you are going to fall.", true));
        this.registerSetting(new ModeSetting("Clutch Mode", "Clutch rotation method.", 1, "Basic", "Advanced") {
            @Override
            public boolean isHidden() {
                return !getBooleanValueFromSettingName("Clutch");
            }
        });

        this.registerSetting(new BooleanSetting("Placement Priority", "Prioritize placement using ALGO_2; defaults to ALGO_1 if disabled.", true));
        this.registerSetting(new BooleanSetting("Place All Attempts", "Attempt to place blocks on all viable positions.", true));

        BLACKLISTED_BLOCKS = Arrays.asList(
                Blocks.AIR,
                Blocks.WATER,
                Blocks.LAVA,
                Blocks.ENCHANTING_TABLE,
                Blocks.BLACK_CARPET,
                Blocks.GLASS_PANE,
                Blocks.IRON_BARS,
                Blocks.ICE,
                Blocks.PACKED_ICE,
                Blocks.CHEST,
                Blocks.TRAPPED_CHEST,
                Blocks.TORCH,
                Blocks.ANVIL,
                Blocks.TRAPPED_CHEST,
                Blocks.NOTE_BLOCK,
                Blocks.JUKEBOX,
                Blocks.TNT,
                Blocks.REDSTONE_WIRE,
                Blocks.LEVER,
                Blocks.COBBLESTONE_WALL,
                Blocks.OAK_FENCE,
                Blocks.TALL_GRASS,
                Blocks.TRIPWIRE,
                Blocks.TRIPWIRE_HOOK,
                Blocks.RAIL,
                Blocks.LILY_PAD,
                Blocks.RED_MUSHROOM,
                Blocks.BROWN_MUSHROOM,
                Blocks.VINE,
                Blocks.ACACIA_TRAPDOOR,
                Blocks.LADDER,
                Blocks.FURNACE,
                Blocks.SAND,
                Blocks.CACTUS,
                Blocks.DISPENSER,
                Blocks.DROPPER,
                Blocks.CRAFTING_TABLE,
                Blocks.COBWEB,
                Blocks.PUMPKIN,
                Blocks.ACACIA_SAPLING
        );
    }

    @Override
    public void onEnable() {
        this.rots = new float[]{mc.player.rotationYaw, 96};
        this.itemPrevEnable = mc.player.inventory.currentItem;
        packetBlockSlot = -1;
        damageTicks = 0;
        this.y = mc.player.getPosY();
        this.blockPos = null;
    }

    public boolean isValid(Item var0) {
        if (!(var0 instanceof BlockItem)) {
            return false;
        } else {
            Block var3 = ((BlockItem) var0).getBlock();
            return !BLACKLISTED_BLOCKS.contains(var3)
                    && !(var3 instanceof AbstractButtonBlock)
                    && !(var3 instanceof BushBlock)
                    && !(var3 instanceof TrapDoorBlock)
                    && !(var3 instanceof AbstractPressurePlateBlock)
                    && !(var3 instanceof SandBlock)
                    && !(var3 instanceof OreBlock)
                    && !(var3 instanceof SkullBlock)
                    && !(var3 instanceof BedBlock)
                    && !(var3 instanceof BannerBlock)
                    && !(var3 instanceof ChestBlock)
                    && !(var3 instanceof DoorBlock);
        }
    }

    public void updateCurrentHand() {
        for (int var3 = 36; var3 < 45; var3++) {
            int var4 = var3 - 36;
            if (mc.player.container.getSlot(var3).getHasStack()
                    && isValid(mc.player.container.getSlot(var3).getStack().getItem())
                    && mc.player.container.getSlot(var3).getStack().getCount() != 0) {
                slot = var4;
                if (mc.player.inventory.currentItem == var4) {
                    return;
                }

                mc.player.inventory.currentItem = var4;
                if (this.getStringSettingValueByName("ItemSpoof").equals("LiteSpoof") && (this.packetBlockSlot < 0 || this.packetBlockSlot != var4)) {
                    mc.getConnection().getNetworkManager().sendPacket(new CHeldItemChangePacket(var4));
                    this.packetBlockSlot = var4;
                }
                break;
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void onMove(EventMove event) {
        if (blockAmount != 0 && !getStringSettingValueByName("Tower Mode").equalsIgnoreCase("None")) {
            tower(event);
        }
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")
                && (!MovementUtil.isMoving() || getBooleanValueFromSettingName("Tower while moving"))) {
            event.cancelled = true;
        }
    }

    public int getBlockAmount() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.player.container.getSlot(i).getHasStack()) {
                ItemStack var5 = mc.player.container.getSlot(i).getStack();
                Item var6 = var5.getItem();
                if (isValid(var6)) {
                    count += var5.getCount();
                }
            }
        }

        return count;
    }

    public int getBlockAmountOnHotbar() {
        int count = 0;

        for (int i = 36; i < 45; i++) {
            if (mc.player.container.getSlot(i).getHasStack()) {
                ItemStack var5 = mc.player.container.getSlot(i).getStack();
                Item var6 = var5.getItem();
                if (isValid(var6)) {
                    count += var5.getCount();
                }
            }
        }

        return count;
    }

    private BlockFlyHelper.BlockCache canPlace(float[] yawPitch) {
        BlockRayTraceResult rayTraceResult;
        RayTraceResult m4 = mc.player.customPick(4.5, 1.0f, yawPitch[0], yawPitch[1]);
        if (m4.getType() == RayTraceResult.Type.BLOCK && (rayTraceResult = (BlockRayTraceResult) m4).getFace() != net.minecraft.util.Direction.DOWN && (placeUp || rayTraceResult.getFace() != net.minecraft.util.Direction.UP)) {
            this.ray = m4.getHitVec();
            return new BlockFlyHelper.BlockCache(rayTraceResult.getPos(), rayTraceResult.getFace());
        }
        return null;
    }

    public void click() {
        BlockPos bp;
        boolean r;
        if (this.blockPos == null) {
            return;
        }
        if (getBlockAmountOnHotbar() == 0) {
            return;
        }


        BlockFlyHelper.BlockCache facing = this.blockPos;

        boolean ex = (int) getNumberValueBySettingName("Extend") != 0;
        r = getBooleanValueFromSettingName("Raytrace");

        if (r) {
            facing = this.canPlace(this.rots);
            if (facing == null || this.ray == null) {

                return;
            }
            this.blockPos = facing;
        }

        if (mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB((bp = this.blockPos.getPosition().add(this.blockPos.getFacing().getXOffset(), this.blockPos.getFacing().getYOffset(), this.blockPos.getFacing().getZOffset())).getX(), bp.getY(), bp.getZ(), bp.getX() + 1, bp.getY() + 1, bp.getZ() + 1)).isEmpty() && !mc.world.getBlockState(bp).isSolid()) {
            if (mc.playerController.processRightClickBlock(mc.player, mc.world, r ? facing.getPosition() : this.blockPos.getPosition(), facing.getFacing(), r ? this.ray : BlockFlyHelper.blockPosRedirection(ex ? new BlockPos(mc.player.getPositionVec()) : this.blockPos.getPosition(), facing.getFacing()), Hand.MAIN_HAND) == ActionResultType.SUCCESS) {
                placedBlocks++;
                if (getStringSettingValueByName("ItemSpoof").equals("Spoof") || this.access().getStringSettingValueByName("ItemSpoof").equals("LiteSpoof")) {
                    mc.player.inventory.currentItem = itemPrevEnable;
                }

            }
            if (getBooleanValueFromSettingName("NoSwing")) {
                mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            } else {
                mc.player.swingArm(Hand.MAIN_HAND);
            }
        }
        mc.sendClickBlockToController(false);
    }

    public void handleBlockPicking() {
        String var3 = this.getStringSettingValueByName("Picking mode");
        if ((!var3.equals("OpenInv") || mc.currentScreen instanceof InventoryScreen) && this.getBlockAmount() != 0) {
            int var4 = 43;
            if (!this.getBooleanValueFromSettingName("Intelligent Block Picker")) {
                if (!this.inventoryFull()) {
                    int var5 = -1;

                    for (int var6 = 9; var6 < 36; var6++) {
                        if (mc.player.container.getSlot(var6).getHasStack()) {
                            Item var7 = mc.player.container.getSlot(var6).getStack().getItem();
                            if (isValid(var7)) {
                                var5 = var6;
                                break;
                            }
                        }
                    }

                    for (int var9 = 36; var9 < 45; var9++) {
                        if (!mc.player.container.getSlot(var9).getHasStack()) {
                            var4 = var9;
                            break;
                        }
                    }

                    if (var5 >= 0) {
                        if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv")) {
                            mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
                        }

                        this.click(var5, var4 - 36);
                        if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv")) {
                            mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                        }
                    }
                }
            } else {
                int var8 = this.bestBlockSlot();
                if (!this.inventoryFull()) {
                    for (int var11 = 36; var11 < 45; var11++) {
                        if (!mc.player.container.getSlot(var11).getHasStack()) {
                            var4 = var11;
                            break;
                        }
                    }
                } else {
                    for (int var10 = 36; var10 < 45; var10++) {
                        if (mc.player.container.getSlot(var10).getHasStack()) {
                            Item var12 = mc.player.container.getSlot(var10).getStack().getItem();
                            if (isValid(var12)) {
                                var4 = var10;
                                if (mc.player.container.getSlot(var10).getStack().getCount()
                                        == mc.player.container.getSlot(var8).getStack().getCount()) {
                                    var4 = -1;
                                }
                                break;
                            }
                        }
                    }
                }

                if (var4 >= 0 && mc.player.container.getSlot(var4).slotNumber != var8) {
                    if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv") && ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_11_1)) {
                        mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
                    }

                    this.click(var8, var4 - 36);
                    if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv")) {
                        mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                    }
                }
            }
        }
    }

    public int bestBlockSlot() {
        int var3 = -1;
        int var4 = 0;
        if (this.getBlockAmount() != 0) {
            for (int var5 = 9; var5 < 45; var5++) {
                if (mc.player.container.getSlot(var5).getHasStack()) {
                    Item var6 = mc.player.container.getSlot(var5).getStack().getItem();
                    ItemStack var7 = mc.player.container.getSlot(var5).getStack();
                    if (isValid(var6) && var7.getCount() > var4) {
                        var4 = var7.getCount();
                        var3 = var5;
                    }
                }
            }

            return var3;
        } else {
            return -1;
        }
    }

    public boolean inventoryFull() {
        for (int var3 = 36; var3 < 45; var3++) {
            if (mc.player.container.getSlot(var3).getHasStack()) {
                Item var4 = mc.player.container.getSlot(var3).getStack().getItem();
                if (isValid(var4)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void click(int var1, int var2) {
        mc.playerController.windowClickFixed(mc.player.container.windowId, var1, var2, ClickType.SWAP, mc.player);
    }


    @EventTarget
    @HighestPriority
    public void onMotion(EventMotion event) {
        if (event.isPre()) {
            if (mc.player.onGround) {
                offGroundTicks = 0;
            } else {
                offGroundTicks++;
            }


            if (damageTicks > 0) {
                if (mc.gameSettings.keyBindSneak.pressed) {
                    mc.gameSettings.keyBindSneak.pressed = false;
                }

                --damageTicks;
            }

            event.setYaw(RotationCore.currentYaw);
            event.setPitch(RotationCore.currentPitch);
        }

        if (getBooleanValueFromSettingName("Sprint")) {
            mc.player.setSprinting(MovementUtil.isMoving());
        } else {
            mc.player.setSprinting(false);
        }
    }

    @EventTarget
    @HighestPriority
    public void onPlayerTick(EventUpdate event) {
        if (getBooleanValueFromSettingName("Eagle") && damageTicks == 0) {
            if (placedBlocks >= (int) getNumberValueBySettingName("Eagle Rate")) {
                boolean block = blockRelativeToPlayer(-0.5, getBooleanValueFromSettingName("Eagle Gap Rand") ? (getNumberValueBySettingName("Eagle Gap") * 2) + RandomUtil.nextFloat(0, 0.3) : (getNumberValueBySettingName("Eagle Gap") * 2)) instanceof AirBlock;
                if (block && mc.player.onGround) {
                    mc.gameSettings.keyBindSneak.pressed = true;
                    this.sneakAfterTick = (int) getNumberValueBySettingName("Eagle Ticks");
                } else if (this.sneakAfterTick > 0) {
                    --this.sneakAfterTick;
                    if (this.sneakAfterTick == 0) {
                        mc.gameSettings.keyBindSneak.pressed = false;
                        placedBlocks = 0;
                    }
                }
            }

        }
        placeUp = (mc.player.fallDistance > 1.2 + 0 || (!MovementUtil.isMoving() &&
                mc.gameSettings.keyBindJump.isKeyDown())) && getBooleanValueFromSettingName("SameY") ||
                !getBooleanValueFromSettingName("SameY");

        handleBlockPicking();
        if (!getStringSettingValueByName("ItemSpoof").equals("None")) {
            updateCurrentHand();
        }

        if (getBooleanValueFromSettingName("SameY")) {
            if (mc.player.fallDistance > 1.2 + 0 || (!MovementUtil.isMoving() && mc.gameSettings.keyBindJump.isKeyDown())) {
                y = mc.player.getPosY();
            }
        } else {
            y = mc.player.getPosY();
        }

        BlockPos downPos = new BlockPos(mc.player.getPosX(), y - 0.5, mc.player.getPosZ());

        boolean shouldExtend = (int) getNumberValueBySettingName("Extend") != 0;
        if (this.blockPos != null && shouldExtend) {
            this.blockPos = new BlockFlyHelper.BlockCache(this.expand(new BlockPos(mc.player.getPositionVector().add(0.0, -0.5, 0.0)), getNumberValueBySettingName("Extend")), this.blockPos.getFacing());
        }

        if (this.blockPos == null || this.slot == -1) {
            return;
        }

        float[] testone = BlockUtils.rotationToBlock(blockPos.getPosition(), blockPos.getFacing());
        float[] testtwo = RotationUtils.scaffoldRots((double) this.blockPos.getPosition().getX() + 0.5 + (double) ((float) this.blockPos.getFacing().getXOffset() / 2.0f), (double) this.blockPos.getPosition().getY() + 0.5 + (double) ((float) this.blockPos.getFacing().getYOffset() / 2.0f), (double) this.blockPos.getPosition().getZ() + 0.5 + (double) ((float) this.blockPos.getFacing().getZOffset() / 2.0f), mc.player.lastReportedYaw, mc.player.lastReportedPitch, getNumberValueBySettingName("Rotation Speed"), getNumberValueBySettingName("Rotation Speed"), false);

        boolean underAir = mc.world.getBlockState(downPos).getBlock() instanceof AirBlock;
        switch (getStringSettingValueByName("Mode")) {
            case "Grim":
                this.rots[0] = underAir ? mc.player.rotationYaw + 180 : mc.player.rotationYaw;
                this.getYawBasedPitch(this.rots[0], true);
                break;

            case "AAC":
                this.rots[0] = testone[0];
                rots[1] = testone[1];
                break;

            case "Hypixel":
                this.rots[0] = mc.player.rotationYaw - 180;
                getYawBasedPitch(rots[0], false);
                break;

            case "SideHit":
                this.rots[0] = mc.player.rotationYaw - 180 + 45;
                this.rots[1] = 77.1f;
                break;

            case "Clutch":
                this.rots[0] = underAir && offGroundTicks > 3 ? mc.player.rotationYaw + 180 : mc.player.rotationYaw;
                getYawBasedPitch(mc.player.rotationYaw - 180, false);
                break;
        }
        BlockFlyHelper.BlockCache f1 = canPlace(this.rots);

        String amode = getStringSettingValueByName("Smart Block Finder");

        boolean shouldAdapt = f1 == null && underAir && !amode.equalsIgnoreCase("Basic");

        float[] var15 = amode.equalsIgnoreCase("Test1") ? testone : testtwo;

        float backSide = shouldAdapt ? var15[0] : (mc.player.rotationYaw + 180);

        if (getStringSettingValueByName("Mode").equalsIgnoreCase("Grim")) {
            if (underAir) {
                rots[0] = backSide;
            }

            if (amode.equalsIgnoreCase("Test1")) {
                rots[1] = shouldAdapt ? var15[1] : rots[1];
            } else if (amode.equalsIgnoreCase("Test2")) {
                if (shouldAdapt) {
                    getYawBasedPitch(rots[0], true);
                }
            }

        }
        if (getStringSettingValueByName("Mode").equalsIgnoreCase("Hypixel")) {

            rots[0] = backSide;

            if (amode.equalsIgnoreCase("Test1")) {
                rots[1] = shouldAdapt ? var15[1] : rots[1];
            } else if (amode.equalsIgnoreCase("Test2")) {
                rots[1] = var15[1];
            }

            getYawBasedPitch(rots[0], false);

            if (getBooleanValueFromSettingName("Static Pitch")) {
                rots[1] = getNumberValueBySettingName("Pitch Value");
            }

        }
        if (getStringSettingValueByName("Mode").equalsIgnoreCase("Clutch")) {
            if (underAir && offGroundTicks > 3) {
                rots[0] = backSide;
            }

            if (amode.equalsIgnoreCase("Test1")) {
                rots[1] = shouldAdapt ? var15[1] : rots[1];
            } else if (amode.equalsIgnoreCase("Test2")) {
                if (shouldAdapt) {
                    getYawBasedPitch(rots[0], false);
                }
            }

        }


        if (damageTicks > 0) {
            if (getStringSettingValueByName("Clutch Mode").equalsIgnoreCase("Basic")) {
                this.rots[0] = testone[0];
                this.rots[1] = testone[1];
            } else if (getStringSettingValueByName("Clutch Mode").equalsIgnoreCase("Advanced")) {
                this.rots[0] = testtwo[0];
                getYawBasedPitch(rots[0], false);

            }
        }

        Rotation limit = RotationUtils.limitAngleChange(new Rotation(mc.player.lastReportedYaw, mc.player.lastReportedPitch), new Rotation(this.rots[0], this.rots[1]), getNumberValueBySettingName("Rotation Speed") + RandomUtil.nextFloat(0.0f, 16.0f), getNumberValueBySettingName("Rotation Speed") + RandomUtil.nextFloat(0.0f, 16.0f));
        this.rots = new float[]{limit.yaw, limit.pitch};

        float[] oldRots = {mc.player.lastReportedYaw, mc.player.lastReportedPitch};
        this.rots = RotationUtils.gcdFix(rots, oldRots);

        RotationCore.currentYaw = rots[0];
        RotationCore.currentPitch = rots[1];


        if (this.slot == -1) {
            return;
        }
        if (this.blockPos != null) {
            this.click();
        }
    }

    public void getYawBasedPitch(float yaw, boolean t) {
        float[] curentRot = null;
        double lastDist = 0.0;
        double diff = 0.3f;
        float y = 40.0f;
        while (y <= 90.0f) {
            block10:
            {
                block9:
                {
                    RayTraceResult rayTraceResult = mc.player.customPick(4.5, 1.0f, yaw, y);
                    if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK) break block9;
                    BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
                    if (!blockRayTraceResult.getFace().equals(this.blockPos.getFacing()) || !blockRayTraceResult.getPos().equals(this.blockPos.getPosition()))
                        break block10;
                    double d = mc.player.getEyePosition(1.0f).squareDistanceTo(blockRayTraceResult.getHitVec());
                    if (curentRot == null || d <= lastDist) {
                        curentRot = RotationUtils.scaffoldRots(blockRayTraceResult.getHitVec().x, blockRayTraceResult.getHitVec().y, blockRayTraceResult.getHitVec().z, mc.player.lastReportedYaw, mc.player.lastReportedPitch, getNumberValueBySettingName("Rotation Speed"), getNumberValueBySettingName("Rotation Speed"), false);
                        lastDist = d;
                    }
                }
                if (y > 70.0f) {
                    diff = 0.05;
                }
            }
            y = (float) ((double) y + diff);
        }
        if (curentRot != null) {
            this.rots[1] = curentRot[1];
        } else if (t) {
            this.rots[1] = 80.41f;
        }
    }

    public BlockPos expand(BlockPos now, double max) {
        double dist = 0.0;
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float YAW = mc.player.rotationYaw;
        BlockPos underPos = now;
        while (BlockFlyHelper.isOkBlock(underPos)) {
            double xCalc = now.getX();
            double zCalc = now.getZ();
            if ((dist += 0.5) > max) break;
            double cos = Math.cos(Math.toRadians(YAW + 90.0f));
            double sin = Math.sin(Math.toRadians(YAW + 90.0f));
            underPos = new BlockPos(xCalc += (forward * cos + strafe * sin) * dist, mc.player.getPosY() - 0.5, zCalc += (forward * sin - strafe * cos) * dist);
        }
        return underPos;
    }

    @EventTarget
    @HighestPriority
    public void onInput(EventMoveButton event) {
        if (getStringSettingValueByName("Mode").equals("Grim")) {
            float current = mc.player.rotationYaw;
            float currentWrapped = MathHelper.wrapAngleTo180_float(current);
            double dir = MathHelper.round(Math.abs(currentWrapped), 45.0F);
            boolean diagonal = dir == 45 || dir == 135;
            BlockPos downPos = new BlockPos(mc.player.getPositionVector().add(0.0, -0.5, 0.0));
            boolean underAir = mc.world.getBlockState(downPos).getBlock() instanceof AirBlock;
            if (underAir && diagonal) {
                //event.sneak = true;
            }
        }

        if (getBooleanValueFromSettingName("AutoJump")) {
            if (MovementUtil.isMoving()) {
                event.jump = true;
            }
        }


        if (getBooleanValueFromSettingName("Verus Yport")) {
            if (MovementUtil.isMoving() && mc.player.isOnGround()) {
                mc.player.setMotion(mc.player.getMotion().x * 1.5, 0.019, mc.player.getMotion().z * 1.5);
            }
        }
    }

    public void tower(EventMove var1) {
        if (mc.timer.timerSpeed == 0.8038576F) {
            mc.timer.timerSpeed = 1.0F;
        }

        if (this.getBlockAmount() != 0 && (!mc.player.collidedVertically
                || this.getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla"))) {
            if (!MovementUtil.isMoving() || this.getBooleanValueFromSettingName("Tower while moving")) {
                String var4 = this.getStringSettingValueByName("Tower Mode");
                switch (var4) {
                    case "NCP":
                        if (var1.getY() > 0.0) {
                            if (MovementUtil.getJumpBoost() == 0) {
                                if (var1.getY() > 0.247 && var1.getY() < 0.249) {
                                    var1.setY(
                                            (double) ((int) (mc.player.getPosY() + var1.getY())) - mc.player.getPosY());
                                }
                            } else {
                                double var6 = (int) (mc.player.getPosY() + var1.getY());
                                if (var6 != (double) ((int) mc.player.getPosY())
                                        && mc.player.getPosY() + var1.getY() - var6 < 0.15) {
                                    var1.setY(var6 - mc.player.getPosY());
                                }
                            }
                        }

                        if (mc.player.getPosY() == (double) ((int) mc.player.getPosY())
                                && BlockUtil.isAboveBounds(mc.player, 0.001F)) {
                            if (mc.gameSettings.keyBindJump.isPressed()) {
                                if (!MovementUtil.isMoving()) {
                                    MovementUtil.moveInDirection(0.0);
                                    MovementUtil.setMotion(var1, 0.0);
                                }

                                var1.setY(MovementUtil.getJumpValue());
                            } else {
                                var1.setY(-1.0E-5);
                            }
                        }
                        break;
                    case "AAC":
                        if (var1.getY() > 0.247 && var1.getY() < 0.249) {
                            var1.setY((double) ((int) (mc.player.getPosY() + var1.getY())) - mc.player.getPosY());
                            if (mc.gameSettings.keyBindJump.isPressed() && !MovementUtil.isMoving()) {
                                MovementUtil.moveInDirection(0.0);
                                MovementUtil.setMotion(var1, 0.0);
                            }
                        } else if (mc.player.getPosY() == (double) ((int) mc.player.getPosY())
                                && BlockUtil.isAboveBounds(mc.player, 0.001F)) {
                            var1.setY(-1.0E-10);
                        }
                        break;
                    case "Vanilla":
                        if (mc.gameSettings.keyBindJump.isPressed()
                                && BlockUtil.isAboveBounds(mc.player, 0.001F)
                                && mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().offset(0.0, 1.0, 0.0))
                                .count() == 0L) {
                            mc.player
                                    .setPosition(mc.player.getPosX(), mc.player.getPosY() + 1.0, mc.player.getPosZ());
                            var1.setY(0.0);
                            MovementUtil.setMotion(var1, 0.0);
                            mc.timer.timerSpeed = 0.8038576F;
                        }
                }
            }
        } else if (!this.getStringSettingValueByName("Tower Mode").equals("AAC")
                || !BlockUtil.isAboveBounds(mc.player, 0.001F)
                || !mc.gameSettings.keyBindJump.isPressed()) {
            if (!this.getStringSettingValueByName("Tower Mode").equals("NCP")
                    && !this.getStringSettingValueByName("Tower Mode").equals("Vanilla")
                    && BlockUtil.isAboveBounds(mc.player, 0.001F)
                    && mc.gameSettings.keyBindJump.isPressed()) {
                mc.player.jumpTicks = 20;
                var1.setY(MovementUtil.getJumpValue());
            }
        } else if (!MovementUtil.isMoving() || this.getBooleanValueFromSettingName("Tower while moving")) {
            mc.player.jumpTicks = 0;
            mc.player.jump();
            MovementUtil.setMotion(var1, MovementUtil.getSmartSpeed());
            MovementUtil.moveInDirection(MovementUtil.getSmartSpeed());
        }

        if (!this.getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")) {
            mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
        }
    }

    @Override
    public String getFormattedName() {
        return Client.getInstance().clientMode != ClientMode.CLASSIC ? super.getFormattedName() : "Scaffold";
    }

    @EventTarget
    public void onTick(EventUpdate var1) {
        if (this.isEnabled()) {
            if (this.getBooleanValueFromSettingName("Show Block Amount")) {
                this.blockAmount = this.getBlockAmount();
            }
        }
    }

    @Override
    public void onDisable() {
        this.animation.changeDirection(Animation.Direction.BACKWARDS);
        if (getStringSettingValueByName("ItemSpoof").equals("Switch")) {
            mc.player.inventory.currentItem = itemPrevEnable;
        }
        damageTicks = 0;
        mc.timer.timerSpeed = 1f;
        if (packetBlockSlot >= 0) {
            mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            packetBlockSlot = -1;
        }
        if (getStringSettingValueByName("Mode").equals("SideHit")) {
            if (mc.gameSettings.keyBindLeft.pressed) {
                mc.gameSettings.keyBindLeft.pressed = false;
            }
        }
        super.onDisable();
    }

    @EventTarget
    public void methwod1we6810(EventSendPacket var1) {
        if (this.isEnabled() && mc.player != null) {
            if (var1.packet instanceof CHeldItemChangePacket && packetBlockSlot >= 0) {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16743(EventRender2DOffset var1) {
        if (mc.player.hurtTime > 0 && getBooleanValueFromSettingName("Clutch")) {
            damageTicks += 20;
        }
        BlockPos downPos = new BlockPos(mc.player.getPosX(), y - 0.5, mc.player.getPosZ());


        this.blockPos = BlockFlyHelper.getBlockCache(downPos, (int) getNumberValueBySettingName("Search"));

        this.animation.changeDirection(Animation.Direction.FORWARDS);
        if (this.animation.calcPercent() != 0.0F) {
            if (this.getBooleanValueFromSettingName("Show Block Amount")) {
                if (Client.getInstance().clientMode != ClientMode.JELLO) {
                    this.renderClassicBlockCount(
                            mc.getMainWindow().getWidth() / 2,
                            mc.getMainWindow().getHeight() / 2 + 15 - (int) (10.0F * this.animation.calcPercent()),
                            this.animation.calcPercent());
                } else {
                    this.renderJelloBlockCount(
                            mc.getMainWindow().getWidth() / 2,
                            mc.getMainWindow().getHeight() - 138
                                    - (int) (25.0F * MathHelper.calculateTransition(this.animation.calcPercent(), 0.0F,
                                    1.0F, 1.0F)),
                            this.animation.calcPercent());
                }
            }
        }
    }

    public static Block blockRelativeToPlayer(double offsetY, double gap) {
        double xCalc = 0, zCalc = 0;
        xCalc = mc.player.getPosX();
        zCalc = mc.player.getPosZ();
        xCalc += (1 * 0.45 * Math.cos(Math.toRadians(mc.player.rotationYaw + 90.0f)) + 0 * 0.45 * Math.sin(Math.toRadians(mc.player.rotationYaw + 90.0f))) * gap;
        zCalc += (1 * 0.45 * Math.sin(Math.toRadians(mc.player.rotationYaw + 90.0f)) - 0 * 0.45 * Math.cos(Math.toRadians(mc.player.rotationYaw + 90.0f))) * gap;
        return mc.world.getBlockState(new BlockPos(xCalc, mc.player.getPosY(), zCalc).add(0, offsetY, 0)).getBlock();
    }

    public void renderClassicBlockCount(int x, int y, float alpha) {
        alpha = (float) (0.5 + 0.5 * (double) alpha);
        GL11.glAlphaFunc(518, 0.1F);
        RenderUtil.drawString(
                Resources.medium17,
                (float) (x + 10),
                (float) (y + 5),
                this.blockAmount + " Blocks",
                RenderUtil.applyAlpha(ClientColors.DEEP_TEAL.getColor(), alpha * 0.3F));
        RenderUtil.drawString(
                Resources.medium17,
                (float) (x + 10),
                (float) (y + 4),
                this.blockAmount + " Blocks",
                RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha * 0.8F));
        GL11.glAlphaFunc(519, 0.0F);
    }

    public void renderJelloBlockCount(int var1, int var2, float var3) {
        int var6 = 0;
        int var7 = ResourceRegistry.JelloLightFont18.getWidth(this.blockAmount + "") + 3;
        var6 += var7;
        var6 += ResourceRegistry.JelloLightFont14.getWidth("Blocks");
        int var8 = var6 + 20;
        int var9 = 32;
        var1 -= var8 / 2;
        GL11.glPushMatrix();
        RenderUtil.method11465(var1, var2, var8, var9, RenderUtil.applyAlpha(-15461356, 0.8F * var3));
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont18, (float) (var1 + 10), (float) (var2 + 4), this.blockAmount + "",
                RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var3));
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14, (float) (var1 + 10 + var7), (float) (var2 + 8), "Blocks",
                RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F * var3));
        var1 += 11 + var8 / 2;
        var2 += var9;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) var1, (float) var2, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var1), (float) (-var2), 0.0F);
        RenderUtil.drawImage((float) var1, (float) var2, 9.0F, 23.0F, Resources.selectPNG,
                RenderUtil.applyAlpha(-15461356, 0.8F * var3));
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
