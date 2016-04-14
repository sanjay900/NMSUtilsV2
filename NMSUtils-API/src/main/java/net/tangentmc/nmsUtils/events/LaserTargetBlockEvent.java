package net.tangentmc.nmsUtils.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tangentmc.nmsUtils.entities.NMSLaser;
@Getter
@Setter
@AllArgsConstructor
public class LaserTargetBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private NMSLaser laser;
	private Block block;
	private boolean goThrough;
	public LaserTargetBlockEvent (NMSLaser laser, Block block) {
		this(laser,block, block.getType().isTransparent()||!block.getType().isSolid());
	}
	public static HandlerList getHandlerList() {      
		return handlers;  
	}
	@Override
	public HandlerList getHandlers() {
		return handlers; 
	}
}