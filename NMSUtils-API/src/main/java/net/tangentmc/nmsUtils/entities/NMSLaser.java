package net.tangentmc.nmsUtils.entities;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface NMSLaser extends NMSEntity{
	default Location getDestination() {
		return getDestinationPart()==null?null:((Entity)getDestinationPart()).getLocation();
	}

	default Location getSource() {
		return getSourcePart()==null?null:((Entity)getSourcePart()).getLocation();
	}
	default boolean isFrozen() { return false; }
	//Lasers are always frozen.
	default void setFrozen(boolean frozen) {}
	NMSLaser getSourcePart();
	NMSLaser getDestinationPart();
	default boolean willSave() { return false; }

	List<NMSLaser> getAllEntities();
}
