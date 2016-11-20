package net.tangentmc.nmsUtils.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.util.Vector;

/**
 * This event is called when an armorstand collides with another entity
 * @author Sanjay
 *
 */
@Getter
@AllArgsConstructor
@ToString
public class EntityCollideWithEntityEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	/**
	 * The entity that was collided with
	 */
	private Entity target;
	/**
	 * The entity that initiated the collision
	 */
	private Entity collider;
	@Setter
	private boolean willCollide = false;
    /**
     * The velocity of the entity that initiated the collision
     */
    private Vector velocity = null;
    public Vector getVelocity() {
        return  velocity==null?collider.getVelocity():velocity;
    }
	public static HandlerList getHandlerList() {      
		return handlers;  
	}
	@Override
	public HandlerList getHandlers() {
		return handlers; 
	}
}
