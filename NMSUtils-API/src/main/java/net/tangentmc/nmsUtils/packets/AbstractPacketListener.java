package net.tangentmc.nmsUtils.packets;

import static com.comphenix.protocol.PacketType.Play.Client.BLOCK_DIG;
import static com.comphenix.protocol.PacketType.Play.Client.BLOCK_PLACE;
import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;
import static com.comphenix.protocol.PacketType.Play.Client.STEER_VEHICLE;
import static com.comphenix.protocol.PacketType.Play.Client.USE_ENTITY;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import net.tangentmc.nmsUtils.NMSUtils;
public abstract class AbstractPacketListener extends PacketAdapter {
	NMSUtils plugin = NMSUtils.getInstance();
	public AbstractPacketListener() {
		super(NMSUtils.getInstance(), ListenerPriority.NORMAL,
				BLOCK_DIG,BLOCK_PLACE,CLIENT_COMMAND,STEER_VEHICLE,USE_ENTITY);
	}
	@Override
	public void onPacketReceiving(final PacketEvent event) {
		if (event.getPacketType() == BLOCK_DIG) {
			onDig(event.getPacket());
		} else if (event.getPacketType() == BLOCK_PLACE) {
			onPlace(event.getPacket());
		} else if (event.getPacketType() == CLIENT_COMMAND) {
			onClientCommand(event.getPacket());
		} else if (event.getPacketType() == STEER_VEHICLE) {
			onSteerVehicle(event.getPacket());
		} else if (event.getPacketType() == USE_ENTITY) {
			onUseEntity(event.getPacket());
		}
	}
	public abstract void onClientCommand(PacketContainer packet);
	public abstract void onDig(PacketContainer packet);
	public abstract void onPlace(PacketContainer packet);
	public abstract void onUseEntity(PacketContainer packet);
	public abstract void onSteerVehicle(PacketContainer packet);
}
