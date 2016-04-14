package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftLingeringPotion;

import net.minecraft.server.v1_9_R1.EntityPotion;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftLingeringPotionEntity extends CraftLingeringPotion implements BasicNMSEntity {

	public CraftLingeringPotionEntity(PotionEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	@Override
	public void setWillSave(boolean b) {
		((PotionEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((PotionEntity)entity).willSave;
	}
	public static class PotionEntity extends EntityPotion implements Collideable{
		public PotionEntity(World world) {
			super(world);
		}
		public PotionEntity(World world, double x, double y, double z,
				net.minecraft.server.v1_9_R1.ItemStack asNMSCopy) {
			super(world,x,y,z,asNMSCopy);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftLingeringPotionEntity(this);
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
}
