package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;

import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.NMSUtilImpl;

public interface BasicNMSEntity extends NMSEntity {
	@Override
	default void spawn() {
		if (!willSave()) {
			NMSUtilImpl.addEntityToWorld(((CraftEntity)this).getHandle().world, ((CraftEntity)this).getHandle());
		}
	}
}
