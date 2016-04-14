package net.tangentmc.nmsUtils.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
@Getter
public class PlayerInteractWithEntityEvent extends Event{
	Player player;
	Entity entity;
	EntityUseAction action;
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() {      
		return handlers;  
	}
	@Override
	public HandlerList getHandlers() {
		return handlers; 
	}

	public enum EntityUseAction {
		ATTACK,INTERACT,INTERACT_AT
	}
}
