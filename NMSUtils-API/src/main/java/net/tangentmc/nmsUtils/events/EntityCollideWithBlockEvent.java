package net.tangentmc.nmsUtils.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
@Getter
public class EntityCollideWithBlockEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Entity entity;
	private Block block;
	private Vector velocity;
	private BlockFace face;
	public static HandlerList getHandlerList() {      
		return handlers;  
	}
	@Override
	public HandlerList getHandlers() {
		return handlers; 
	}
}
