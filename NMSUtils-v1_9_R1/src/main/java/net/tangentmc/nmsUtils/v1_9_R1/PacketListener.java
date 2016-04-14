package net.tangentmc.nmsUtils.v1_9_R1;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ClientCommand;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;

import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.PacketPlayInUseEntity;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.events.PlayerInteractWithEntityEvent;
import net.tangentmc.nmsUtils.events.PlayerInteractWithEntityEvent.EntityUseAction;
import net.tangentmc.nmsUtils.events.PlayerPushedKeyEvent;
import net.tangentmc.nmsUtils.packets.Key;

public class PacketListener extends PacketAdapter implements Listener{
	NMSUtilImpl util;
	public PacketListener(NMSUtilImpl util) {
		super(NMSUtils.getInstance(), ListenerPriority.NORMAL,
				PacketType.Play.Client.BLOCK_DIG,
				PacketType.Play.Client.BLOCK_PLACE,
				PacketType.Play.Client.CLIENT_COMMAND,
				PacketType.Play.Client.STEER_VEHICLE,
				PacketType.Play.Client.USE_ENTITY);
		this.util = util;
		Bukkit.getPluginManager().registerEvents(this, NMSUtils.getInstance());
	}
	@Override
	public void onPacketReceiving(final PacketEvent event) {
		PacketContainer packet = event.getPacket();
		if (packet.getType() == PacketType.Play.Client.USE_ENTITY) {
			PacketPlayInUseEntity raw = (PacketPlayInUseEntity) packet.getHandle();
			Entity interactedWith = raw.a(((CraftWorld)event.getPlayer().getWorld()).getHandle());
			com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
			PlayerInteractWithEntityEvent evt = new PlayerInteractWithEntityEvent(event.getPlayer(),interactedWith.getBukkitEntity(), EntityUseAction.valueOf(action.name()));
			Bukkit.getPluginManager().callEvent(evt);		
		}
		//This way, normal horses and stuff wont be affected and will function normally
		if (!util.getRiding().contains(event.getPlayer().getUniqueId())) return;
		if (packet.getType() == PacketType.Play.Client.CLIENT_COMMAND) {
			if (packet.getClientCommands().read(0) != ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) return;
			PlayerPushedKeyEvent pmEvent = new PlayerPushedKeyEvent(event.getPlayer(), Arrays.asList(Key.OPEN_INVENTORY));
			Bukkit.getPluginManager().callEvent(pmEvent);
			if (pmEvent.isCancelled()) {
				event.getPlayer().closeInventory();
			}
			return;
		}
		Cancellable pmEvent = null;
		if (packet.getType() == PacketType.Play.Client.BLOCK_DIG) {
			if (packet.getPlayerDigTypes().read(0) == PlayerDigType.DROP_ITEM||packet.getPlayerDigTypes().read(0) == PlayerDigType.DROP_ALL_ITEMS)
			pmEvent = new PlayerPushedKeyEvent(event.getPlayer(), Arrays.asList(Key.DROP_ITEM));
			if (packet.getPlayerDigTypes().read(0) == PlayerDigType.START_DESTROY_BLOCK)
			pmEvent = new PlayerPushedKeyEvent(event.getPlayer(), Arrays.asList(Key.BREAK));
		}
		if (packet.getType() == PacketType.Play.Client.BLOCK_PLACE) {
			pmEvent = new PlayerPushedKeyEvent(event.getPlayer(), Arrays.asList(Key.PLACE));
		}
		if (packet.getType() == PacketType.Play.Client.STEER_VEHICLE) {
			final float sideMot = packet.getFloat().read(0);
			final float forMot = packet.getFloat().read(1);
			boolean jump = packet.getBooleans().read(0);
			boolean unmount = packet.getBooleans().read(1);
			//avoid unmounting players when sneak is pressed
			pmEvent = new PlayerPushedKeyEvent(event.getPlayer(),forMot,sideMot,jump,unmount);
			event.setCancelled(pmEvent.isCancelled());
		}
		if (pmEvent == null) return;
		boolean isUnmount = event.isCancelled();
		Bukkit.getPluginManager().callEvent((Event) pmEvent);
		event.setCancelled(isUnmount || pmEvent.isCancelled());
	}
}
