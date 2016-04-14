package net.tangentmc.nmsUtils.v1_8_R3.entities.basic;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.v1_8_R3.NMSUtilImpl;

public interface BasicNMSEntity extends NMSEntity {
	@Override
	default void spawn() {
		if (!willSave()) {
			NMSUtilImpl.addEntityToWorld(((CraftEntity)this).getHandle().world, ((CraftEntity)this).getHandle());
		}
	}
}
