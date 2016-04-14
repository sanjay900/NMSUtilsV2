package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_9_R1.EntityItem;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldServer;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftItemEntity extends CraftItem implements BasicNMSEntity {

	public CraftItemEntity(ItemEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	@Override
	public void setWillSave(boolean b) {
		((ItemEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((ItemEntity)entity).willSave;
	}
	public static class ItemEntity extends EntityItem implements Collideable{
		public ItemEntity(World world) {
			super(world);
		}
		public ItemEntity(World world, Location loc, ItemStack data) {
			super(world,loc.getX(), loc.getY(),loc.getZ(), CraftItemStack.asNMSCopy(data));
		}
		public ItemEntity(WorldServer handle, double x, double y, double z,
				net.minecraft.server.v1_9_R1.ItemStack asNMSCopy) {
			super(handle,x,y,z,asNMSCopy);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftItemEntity(this);
		}
		boolean frozen = false;
		@Override
		public void m() {
			this.testCollision();
			if (!frozen)
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
		((ItemEntity)entity).frozen = b;
	}
	@Override
	public boolean isFrozen() {
		return ((ItemEntity)entity).frozen;
	}
}
