package net.tangentmc.nmsUtils.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.tangentmc.nmsUtils.entities.NMSEntity;
@Getter
@AllArgsConstructor
public class NMSEntitySpawnEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private NMSEntity entity;
	public static HandlerList getHandlerList() {      
		return handlers;  
	}
	@Override
	public HandlerList getHandlers() {
		return handlers; 
	}
}
