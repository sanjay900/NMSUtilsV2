package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftExperienceOrb;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.minecraft.server.v1_9_R1.EntityExperienceOrb;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftExperienceOrbEntity extends CraftExperienceOrb implements BasicNMSEntity {

	public CraftExperienceOrbEntity(ExperienceOrbEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	public void setWillSave(boolean b) {
		((ExperienceOrbEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((ExperienceOrbEntity)entity).willSave;
	}
	public static class ExperienceOrbEntity extends EntityExperienceOrb implements Collideable{
		public ExperienceOrbEntity(World world) {
			super(world);
		}
		public ExperienceOrbEntity(World world, double x, double y, double z, int i) {
			super(world,x,y,z,i);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftExperienceOrbEntity(this);
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
