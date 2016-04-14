package net.tangentmc.nmsUtils.v1_9_R1.entities;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftArmorStandEntity.ArmorStandEntity;


public class FrozenStandEntity extends ArmorStandEntity {
	Entity parent;
	public FrozenStandEntity(Entity parent, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.parent = parent;
		setMarker(true);
		setGravity(false);
		setInvisible(true);
		setSmall(true);
		NMSUtilImpl.addEntityToWorld(world, this);
	}
	public FrozenStandEntity(Player who, Location add) {
		this(((CraftEntity)who).getHandle(), ((CraftWorld)add.getWorld()).getHandle(), add.getX(),add.getY(),add.getZ());
	}
	@Override
	public void m() {
		//super.m();
		parent.startRiding(this);
	}


}
