package net.tangentmc.nmsUtils.v1_8_R3.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import net.tangentmc.nmsUtils.entities.NMSArmorStand;
import net.tangentmc.nmsUtils.v1_8_R3.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_8_R3.entities.effects.Collideable;

public class CraftArmorStandEntity extends CraftArmorStand implements NMSArmorStand {
	
	public CraftArmorStandEntity(ArmorStandEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	//For whatever reason, this needs to be overridden...
	@Override
	public boolean teleport(Location location, TeleportCause cause) {
		if (entity.passenger != null || entity.dead) {
            return false;
        }

        // If this entity is riding another entity, we must dismount before teleporting.
        entity.mount(null);

        entity.world = ((CraftWorld) location.getWorld()).getHandle();
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        // entity.setLocation() throws no event, and so cannot be cancelled
        return true;
	}
	public static class ArmorStandEntity extends EntityArmorStand implements Collideable{
		
		public ArmorStandEntity(World world) {
			super(world);
		}
		public ArmorStandEntity(World world, double x, double y, double z) {
			super(world,x,y,z);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftArmorStandEntity(this);
		}
		@Override
		public void t_() {
			super.t_();
			this.testMovement();
			this.testCollision();

		}
		boolean willSave = true;
		@Override
		public boolean d(NBTTagCompound nbttagcompound) { 
			return willSave?super.d(nbttagcompound):false;
		}
		public void lock() {
			NBTTagCompound tag = NMSUtilImpl.getTag(this);
			tag.setInt("DisabledSlots", 2039583);
			a(tag);
		}
		public void unlock() {
			NBTTagCompound tag = NMSUtilImpl.getTag(this);
			tag.setInt("DisabledSlots", 0);
			a(tag);
		}
	}
	@Override
	public void lock() {
		NBTTagCompound tag = NMSUtilImpl.getTag(entity);
		tag.setInt("DisabledSlots", 2039583);
		((ArmorStandEntity)entity).a(tag);
	}
	@Override
	public void unlock() {
		NBTTagCompound tag = NMSUtilImpl.getTag(entity);
		tag.setInt("DisabledSlots", 0);
		((ArmorStandEntity)entity).a(tag);
	}
	@Override
	public void setFrozen(boolean b) {
		this.setGravity(!b);
	}
	@Override
	public boolean isFrozen() {
		return !this.hasGravity();
	}
	@Override
	public void setWillSave(boolean b) {
		((ArmorStandEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((ArmorStandEntity)entity).willSave;
	}
	@Override
	public void spawn() {
		if (!this.willSave()) {
			NMSUtilImpl.addEntityToWorld(entity.world, entity);
		}
	}
	
}
