package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftTNTPrimed;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.EntityTNTPrimed;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftTNTPrimedEntity extends CraftTNTPrimed implements BasicNMSEntity {

	public CraftTNTPrimedEntity(TNTPrimedEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	

	@Override
	public void setWillSave(boolean b) {
		((TNTPrimedEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((TNTPrimedEntity)entity).willSave;
	}
	public static class TNTPrimedEntity extends EntityTNTPrimed implements Collideable{
		public TNTPrimedEntity(World world) {
			super(world);
		}
		public TNTPrimedEntity(World world, double x, double y, double z, EntityLiving entityliving) {
			super(world, x, y, z, entityliving);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftTNTPrimedEntity(this);
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
			return willSave && super.c(nbttagcompound);
		}
		@Override
		public boolean d(NBTTagCompound nbttagcompound) { 
			return willSave && super.d(nbttagcompound);
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
}
