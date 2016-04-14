package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftFirework;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.minecraft.server.v1_9_R1.EntityFireworks;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftFireworksEntity extends CraftFirework implements BasicNMSEntity {

	public CraftFireworksEntity(FireworksEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	public static class FireworksEntity extends EntityFireworks implements Collideable{
		public FireworksEntity(World world) {
			super(world);
		}
		public FireworksEntity(World world, double x, double y, double z, ItemStack object) {
			super(world,x,y,z,object);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftFireworksEntity(this);
		}
		@Override
		public void m() {
			this.testCollision();
			super.m();
			this.testMovement();

		}

		boolean willSave = true;
		@Override
		public void a(NBTTagCompound nbttagcompound) {
			if (willSave) super.a(nbttagcompound);
		}
		@Override
		public void b(NBTTagCompound nbttagcompound) {
			if (willSave) super.b(nbttagcompound);
		}
		@Override
		public boolean c(NBTTagCompound nbttagcompound) {
			return willSave?super.c(nbttagcompound):false;
		}
		@Override
		public boolean d(NBTTagCompound nbttagcompound) { 
			return willSave?super.d(nbttagcompound):false;
		}
		@Override
		public void e(NBTTagCompound nbttagcompound) {
			if (willSave) super.e(nbttagcompound);
		}
	}
	@Override
	public void setFrozen(boolean b) {

	}
	@Override
	public boolean isFrozen() {
		return false;
	}
	public void setWillSave(boolean b) {
		((FireworksEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((FireworksEntity)entity).willSave;
	}
}
