package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.entities.NMSArmorStand;
import net.tangentmc.nmsUtils.v1_9_R1.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftArmorStandEntity extends CraftArmorStand implements NMSArmorStand {
	
	public CraftArmorStandEntity(ArmorStandEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	public static class ArmorStandEntity extends EntityArmorStand implements Collideable{
		
		public ArmorStandEntity(World world) {
			super(world);
		}
		public ArmorStandEntity(World world, double x, double y, double z) {
			super(world,x,y,z);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftArmorStandEntity(this);
		}
		@Override
		public void m() {
			super.m();
			this.testMovement();
			this.testCollision();

		}
		boolean willSave = true;
		@Override
		public boolean d(NBTTagCompound nbttagcompound) { 
			return willSave && super.d(nbttagcompound);
		}
		public void lock() {
			NBTTagCompound tag = NMSUtilImpl.getTag(this);
			tag.setInt("DisabledSlots", 2039583);
			a(tag);
		}
		public void unlock() {
			NBTTagCompound tag = NMSUtilImpl.getTag(this);
			tag.setInt("DisabledSlots", 0);
			a(tag);
		}
	}
	@Override
	public void lock() {
		NBTTagCompound tag = NMSUtilImpl.getTag(entity);
		tag.setInt("DisabledSlots", 2039583);
		((ArmorStandEntity)entity).a(tag);
	}
	@Override
	public void unlock() {
		NBTTagCompound tag = NMSUtilImpl.getTag(entity);
		tag.setInt("DisabledSlots", 0);
		((ArmorStandEntity)entity).a(tag);
	}
	@Override
	public void setFrozen(boolean b) {
		this.setGravity(!b);
	}
	@Override
	public boolean isFrozen() {
		return !this.hasGravity();
	}
	@Override
	public void setWillSave(boolean b) {
		((ArmorStandEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((ArmorStandEntity)entity).willSave;
	}
	@Override
	public void spawn() {
		if (!this.willSave()) {
			NMSUtilImpl.addEntityToWorld(entity.world, entity);
		}
	}
	
}
