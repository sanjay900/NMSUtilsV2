package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftFallingSand;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.EntityFallingBlock;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftArmorStandEntity.ArmorStandEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftFallingBlockEntity extends CraftFallingSand implements BasicNMSEntity {

	public CraftFallingBlockEntity(FallingBlockEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	public void setWillSave(boolean b) {
		((FallingBlockEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((FallingBlockEntity)entity).willSave;
	}
	public static class FallingBlockEntity extends EntityFallingBlock implements Collideable{
		public FallingBlockEntity(World world) {
			super(world);
		}
		public FallingBlockEntity(World world, Location loc, ItemStack stack) {
			super(world, loc.getX(),loc.getY(),loc.getZ(),Block.getById(stack.getTypeId()).fromLegacyData(stack.getData().getData()));
		}
		public FallingBlockEntity(World world, double d, double e, double f, IBlockData fromLegacyData) {
			super(world,d,e,f,fromLegacyData);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftFallingBlockEntity(this);
		}
		@Override
		public void m() {
			if (this.isFrozen()) {
				en.a(new AxisAlignedBB(locX-0.5,en.locY,locZ-0.5,locX+0.5,en.locY+0.1,locZ+0.5));
				this.startRiding(en);
				en.setGravity(false);
				en.setMarker(true);
			}
			this.testCollision();
			if (!this.frozen)
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
		@Getter
		private boolean frozen;
		ArmorStandEntity en;
		public void setFrozen(boolean b) {
			if (frozen == b) return;
			frozen = b;
			if (b) {
				if (en != null) en.getBukkitEntity().remove();
				en = new ArmorStandEntity(world);
				en.setLocation(locX, locY, locZ, yaw, pitch);
				en.setInvisible(true);
				en.setSmall(true);
				en.setGravity(true);
				en.setMarker(true);
				NMSUtilImpl.addEntityToWorld(world, en);
				this.startRiding(en);
			} else {
				if (en != null) en.getBukkitEntity().remove();
			}

		}

	}

	@Override
	public void setFrozen(boolean b) {
		((FallingBlockEntity) entity).setFrozen(b);
	}

	@Override
	public boolean isFrozen() {
		return ((FallingBlockEntity) entity).isFrozen();
	}
}
