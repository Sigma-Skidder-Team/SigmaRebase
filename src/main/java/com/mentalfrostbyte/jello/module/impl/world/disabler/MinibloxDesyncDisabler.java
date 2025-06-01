package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.Objects;

public class MinibloxDesyncDisabler extends Module {
	public MinibloxDesyncDisabler() {
		super(ModuleCategory.EXPLOIT, "Miniblox Desync", "Sends C0CInput packets to fix desyncs (not needed anymore, they reverted to the old ac)");
	}

	@EventTarget
	public void onMove(EventSendPacket event) {
		if (event.packet instanceof CPlayerPacket) {
			assert mc.player != null;
			Objects.requireNonNull(mc.getConnection()).sendPacket(new CInputPacket(
					mc.player.moveStrafing,
					mc.player.moveForward,
					mc.player.isJumping,
					mc.player.isSneaking()
			));
		}
	}
}
