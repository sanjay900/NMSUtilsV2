package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;

import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.EntityEnderCrystal;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftEnderCrystalEntity extends CraftEnderCrystal implements BasicNMSEntity {

	public CraftEnderCrystalEntity(EnderCrystalEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	public void setWillSave(boolean b) {
		((EnderCrystalEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((EnderCrystalEntity)entity).willSave;
	}
	public static class EnderCrystalEntity extends EntityEnderCrystal implements Collideable{
		public EnderCrystalEntity(World world) {
			super(world);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftEnderCrystalEntity(this);
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
		public void showBottom(boolean b) {
			this.a(b);
		}
		public boolean isBottomShowing() {
			return k();
		}
		public void targetLocation(Location target) {
			a(new BlockPosition(target.getBlockX(),target.getBlockY(),target.getBlockZ()));
		}
	}
	public void targetLocation(Location target) {
		((EnderCrystalEntity)entity).targetLocation(target);
	}
	public void showBottom(boolean b) {
		((EnderCrystalEntity)entity).showBottom(b);
	}
	public boolean isBottomShowing() {
		return ((EnderCrystalEntity)entity).isBottomShowing();
	}
	//Endercrystals dont fall...
	@Override
	public void setFrozen(boolean b) {	}
	@Override
	public boolean isFrozen() {
		return true;
	}
}
