package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftOcelot;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_9_R1.EntityOcelot;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

public class CraftOcelotEntity extends CraftOcelot implements BasicNMSEntity {
	
	public CraftOcelotEntity(OcelotEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	public void setFrozen(boolean b) {
		((OcelotEntity) entity).setFrozen(b);
	}
	public boolean isFrozen() {
		return ((OcelotEntity) entity).isFrozen();
	}

	@Override
	public void setWillSave(boolean b) {
		((OcelotEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((OcelotEntity)entity).willSave;
	}
	public static class OcelotEntity extends EntityOcelot implements Collideable{
		@Getter
		@Setter
		boolean frozen;
		public OcelotEntity(World world) {
			super(world);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftOcelotEntity(this);
		}
		@Override
		public void m() {
			this.testCollision();
			super.m();
			this.testMovement();
		}
		@Override
		public void n() {
			if (!frozen) {
				super.n();
			}
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
