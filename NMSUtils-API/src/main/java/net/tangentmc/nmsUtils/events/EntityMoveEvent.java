package net.tangentmc.nmsUtils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
@Getter
@AllArgsConstructor
public class EntityMoveEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Entity entity;
	private double lastX;
	private double lastY;
	private double lastZ;
	private double locX;
	private double locY;
	private double locZ;
	private float pitch;
	private float lastPitch;
	private float yaw;
	private float lastYaw;
	/**
	 * Get the Location this event originated at
	 * @return Location this event originated at
	 */
	public Location getFrom() {
		return new Location(getWorld(),lastX,lastY,lastZ,lastYaw,lastPitch);
	}
	/**
	 * Get the Location this event goes to
	 * @return Location this event originated at
	 */
	public Location getTo() {
		return new Location(getWorld(),locX,locY,locZ,yaw,pitch);
	}
	/**
	 * Gets the world in which the entity moved
	 * 
	 * @return Last and current Entity World
	 */
	public World getWorld() {
		return this.entity.getWorld();
	}
	public static HandlerList getHandlerList() {      
		return handlers;  
	}
	@Override
	public HandlerList getHandlers() {
		return handlers; 
	}
}
