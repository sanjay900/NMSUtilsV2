package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftAreaEffectCloud;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_9_R1.EntityAreaEffectCloud;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldServer;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftShulkerEntity.ShulkerEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftAreaEffectCloudEntity extends CraftAreaEffectCloud implements BasicNMSEntity {
	
	public CraftAreaEffectCloudEntity(AreaEffectCloudEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	public void setFrozen(boolean b) {
		((AreaEffectCloudEntity) entity).setFrozen(b);
	}
	public boolean isFrozen() {
		return ((AreaEffectCloudEntity) entity).isFrozen();
	}
	@Override
	public void setWillSave(boolean b) {
		((AreaEffectCloudEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((AreaEffectCloudEntity)entity).willSave;
	}
	public static class AreaEffectCloudEntity extends EntityAreaEffectCloud implements Collideable{
		@Getter
		@Setter
		boolean frozen;
		public AreaEffectCloudEntity(World world) {
			super(world);
		}
		public AreaEffectCloudEntity(WorldServer world, double x, double y, double z) {
			super(world,x,y,z);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftAreaEffectCloudEntity(this);
		}
		@Override
		public void m() {
			this.testCollision();
			super.m();
			this.testMovement();
		}
		@Override
		public void g(double d0, double d1, double d2) {
			if (!frozen) {
				super.g(d0,d1,d2);
			}
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
}
