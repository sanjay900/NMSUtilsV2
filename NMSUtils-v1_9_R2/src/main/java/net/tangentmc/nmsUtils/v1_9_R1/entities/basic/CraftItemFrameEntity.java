package net.tangentmc.nmsUtils.v1_9_R1.entities.basic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftItemFrame;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import lombok.ToString;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.EntityItemFrame;
import net.minecraft.server.v1_9_R1.EnumDirection;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.World;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSItemFrame;
import net.tangentmc.nmsUtils.v1_9_R1.NMSUtilImpl;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;

@ToString
public class CraftItemFrameEntity extends CraftItemFrame implements NMSItemFrame {
	
	public CraftItemFrameEntity(ItemFrameEntity entity) {
		super((CraftServer) Bukkit.getServer(), entity);
	}
	
	@Override
	public void setWillSave(boolean b) {
		((ItemFrameEntity)entity).willSave = b;
	}
	@Override
	public boolean willSave() {
		return ((ItemFrameEntity)entity).willSave;
	}
	@Override
	public void setImage(String image) {
		NMSUtils.getInstance().getMap().swapImage(this, image);
	}
	public static class ItemFrameEntity extends EntityItemFrame implements Collideable{
		public ItemFrameEntity(World world) {
			super(world);
		}
		public ItemFrameEntity(World world, BlockPosition blockPosition, EnumDirection dir) {
			super(world,blockPosition,dir);
		}
		@Override
		public CraftEntity getBukkitEntity() {
			return new CraftItemFrameEntity(this);
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
		//I mean, they dont move so...
		return true;
	}
	@Override
	public void spawn() {
		if (!this.willSave()) {
			NMSUtilImpl.addEntityToWorld(entity.world, entity);
		}
	}
}
