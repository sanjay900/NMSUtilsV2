package net.tangentmc.nmsUtils.v1_8_R3.entities;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;
import net.tangentmc.nmsUtils.v1_8_R3.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_8_R3.entities.basic.CraftArmorStandEntity.ArmorStandEntity;


public class FrozenStandEntity extends ArmorStandEntity {
	Entity parent;
	public FrozenStandEntity(Entity parent, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.parent = parent;
		n(true);
		setGravity(false);
		setInvisible(true);
		setSmall(true);
		NMSUtilImpl.addEntityToWorld(world, this);
	}
	@Override
	public void t_() {
		super.t_();
		parent.mount(this);
	}


}
