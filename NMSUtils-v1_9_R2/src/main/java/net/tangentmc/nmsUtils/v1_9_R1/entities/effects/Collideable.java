package net.tangentmc.nmsUtils.v1_9_R1.entities.effects;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityProjectile;
import net.minecraft.server.v1_9_R1.MovingObjectPosition;
import net.minecraft.server.v1_9_R1.Vec3D;
import net.tangentmc.nmsUtils.events.EntityCollideWithBlockEvent;
import net.tangentmc.nmsUtils.events.EntityCollideWithEntityEvent;
import net.tangentmc.nmsUtils.events.EntityMoveEvent;
import net.tangentmc.nmsUtils.utils.FaceUtil;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.BlockHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.BlockHologramEntity.HologramBlock;

public interface Collideable {
	default void testMovement() {
		Entity en = (Entity) this;
		org.bukkit.entity.Entity e = en.getBukkitEntity();
		if (en.locX != en.lastX || en.locY != en.lastY || en.locZ != en.lastZ || en.pitch != en.lastPitch || en.yaw != en.lastYaw) {
			EntityMoveEvent evt = new EntityMoveEvent(e, en.lastX, en.lastY, en.lastZ, en.locX, en.locY, en.locZ, en.pitch, en.lastPitch, en.yaw, en.lastYaw);
			Bukkit.getPluginManager().callEvent(evt);
		}
	}
	default void testCollision(Entity... ignore) {
		Entity en = (Entity) this;
		org.bukkit.entity.Entity e = en.getBukkitEntity();
		AxisAlignedBB bb = en.getBoundingBox();
		if (en.getBukkitEntity().hasMetadata("sizeY")) {
			double d = en.getBukkitEntity().getMetadata("sizeY").get(0).asDouble();
			double d2 = en.locY;
			bb = new AxisAlignedBB(bb.a,d2,bb.c,bb.d,d2+d,bb.f);
		}
		List<Entity> list = en.world.getEntities(en, bb.grow(0.3, 0, 0.3));
		if (en instanceof EntityProjectile) {
			list.remove(((EntityProjectile)en).getShooter());
		}
		if (en.getVehicle() != null) list.remove(en.getVehicle());
		list.removeAll(en.passengers);

		list.removeAll(Arrays.asList(ignore));

		for (int i = 0; i < list.size(); ++i) {
			Entity entity1 = list.get(i);
			EntityCollideWithEntityEvent ev = new EntityCollideWithEntityEvent(e, entity1.getBukkitEntity(),false);
			Bukkit.getPluginManager().callEvent(ev);
			if (ev.isWillCollide()) {
				entity1.collide(en);
			}
		}
		double y = en.locY;
		if (Math.abs(en.motY) < 0.09) {
			y = Math.round(en.locY);
		}
		//The y value needs to be rounded, as Vec3d doesnt do it for you, and that leads to errors.
		Vec3D vec3d = new Vec3D(en.locX, y, en.locZ);
		double x = 0;
		double z = 0;
		if (en.motX != 0) x = en.motX >0.05?0.5:en.motX<-0.05?-0.5:0;
		if (en.motZ != 0) z = en.motZ >0.05?0.5:en.motZ<-0.05?-0.5:0;
		Vec3D vec3d1 = new Vec3D(en.locX + x, y + (Math.abs(en.motY) < 0.09?0:en.motY), en.locZ + z);
		MovingObjectPosition movingobjectposition = en.world.rayTrace(vec3d, vec3d1);


		if (movingobjectposition == null) {
			vec3d1 = new Vec3D(en.locX, vec3d.y, en.locZ);
			movingobjectposition = en.world.rayTrace(vec3d, vec3d1);
		}
		if (movingobjectposition != null) {
			BlockPosition bp = movingobjectposition.a();
			
			Block b = e.getWorld().getBlockAt(bp.getX(), bp.getY(), bp.getZ());
			BlockFace bf = FaceUtil.getDirection(b.getLocation().toVector().subtract(en.getBukkitEntity().getLocation().getBlock().getLocation().toVector()));
			EntityCollideWithBlockEvent evt = new EntityCollideWithBlockEvent(e, b,e.getVelocity(),bf);
			Bukkit.getPluginManager().callEvent(evt);

		} 
	}
}
