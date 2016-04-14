package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftMinecart;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.minecraft.server.v1_9_R1.EntityMinecartMobSpawner;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftMinecartMobSpawnerEntity extends CraftMinecart implements SpawnerMinecart, BasicNMSEntity {

	public CraftMinecartMobSpawnerEntity(MinecartMobSpawnerEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	@Override
    public String toString() {
        return "CraftMinecartMobSpawner";
    }

    public EntityType getType() {
        return EntityType.MINECART_MOB_SPAWNER;
    }

	@Override
	public void setWillSave(boolean b) {
		((MinecartMobSpawnerEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((MinecartMobSpawnerEntity)entity).willSave;
	}
	public static class MinecartMobSpawnerEntity extends EntityMinecartMobSpawner implements Collideable{
		public MinecartMobSpawnerEntity(World world) {
			super(world);
		}
		public MinecartMobSpawnerEntity(World world, double x, double y, double z) {
			super(world,x,y,z);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftMinecartMobSpawnerEntity(this);
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
