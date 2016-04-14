package net.tangentmc.nmsUtils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
@Getter
@AllArgsConstructor
public class EntityDespawnEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Entity entity;	
	public static HandlerList getHandlerList() {      
		return handlers;  
	}
	@Override
	public HandlerList getHandlers() {
		return handlers; 
	}
}
