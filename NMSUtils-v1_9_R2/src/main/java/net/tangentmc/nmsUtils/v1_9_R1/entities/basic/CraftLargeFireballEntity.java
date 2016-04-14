package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftLargeFireball;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.minecraft.server.v1_9_R1.EntityLargeFireball;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftLargeFireballEntity extends CraftLargeFireball implements BasicNMSEntity {

	public CraftLargeFireballEntity(LargeFireballEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	@Override
	public void setWillSave(boolean b) {
		((LargeFireballEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((LargeFireballEntity)entity).willSave;
	}
	public static class LargeFireballEntity extends EntityLargeFireball implements Collideable{
		public LargeFireballEntity(World world) {
			super(world);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftLargeFireballEntity(this);
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
