package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;

import net.minecraft.server.v1_9_R1.EntityTippedArrow;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftTippedArrowEntity extends CraftArrow implements BasicNMSEntity {

	public CraftTippedArrowEntity(TippedArrowEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	public static class TippedArrowEntity extends EntityTippedArrow implements Collideable{
		public TippedArrowEntity(World world) {
			super(world);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftTippedArrowEntity(this);
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
	@Override
	public void setWillSave(boolean b) {
		((TippedArrowEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((TippedArrowEntity)entity).willSave;
	}
}
