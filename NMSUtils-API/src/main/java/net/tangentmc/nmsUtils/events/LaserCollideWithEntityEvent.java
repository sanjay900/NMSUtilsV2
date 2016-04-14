package net.tangentmc.nmsUtils.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import lombok.Getter;
import lombok.Setter;
public class LaserCollideWithEntityEvent extends EntityCollideWithEntityEvent {
	@Getter
	@Setter
	Location newLoc = null;
	public LaserCollideWithEntityEvent(Entity stand, Entity target) {
		super(stand, target, false);
	}
	@Override
	public String toString() {
		return "LaserCollideWithEntity"+super.toString();
	}

}
