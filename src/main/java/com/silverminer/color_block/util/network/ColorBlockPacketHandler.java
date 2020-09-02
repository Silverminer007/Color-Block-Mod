package com.silverminer.color_block.util.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.ColorBlockMod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ColorBlockPacketHandler {

	protected static final Logger LOGGER = LogManager.getLogger(ColorBlockPacketHandler.class);
	private static final String PROTOCOL_VERSION = "1.0";
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ColorBlockMod.MODID, "main_channel"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();

	public static void register() {
		CHANNEL.registerMessage(0, SColorChangePacket.class, SColorChangePacket::encode, SColorChangePacket::decode,
				SColorChangePacket::handle);

		CHANNEL.registerMessage(1, CColorChangePacket.class, CColorChangePacket::encode, CColorChangePacket::decode,
				CColorChangePacket::handle);

		CHANNEL.registerMessage(2, ImageDataPacket.class, ImageDataPacket::encode, ImageDataPacket::decode,
				ImageDataPacket::handle);
	}

	public static void sendTo(Object message, PlayerEntity player) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
	}

	public static void sendToAll(Object message) {
		CHANNEL.send(PacketDistributor.ALL.noArg(), message);
	}

	public static void sendToServer(Object message) {
		CHANNEL.sendToServer(message);
	}
}