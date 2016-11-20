package net.tangentmc.nmsUtils.v1_10_R1.entities.effects;

import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityProjectile;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.events.EntityCollideWithEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Collideable {
    public static void testCollision(Entity en) {
        testCollision(en, new ArrayList<>());
    }
	public static void testCollision(Entity en, List<Entity> ignore) {
		org.bukkit.entity.Entity e = en.getBukkitEntity();
		AxisAlignedBB bb = en.getBoundingBox();
		if (en.getBukkitEntity().hasMetadata(NMSEntity.BOUNDING_TAG)) {
			Vector v = (Vector) en.getBukkitEntity().getMetadata(NMSEntity.BOUNDING_TAG).get(0).value();
			bb = new AxisAlignedBB(en.locX-v.getX(),en.locY-v.getY(),en.locZ-v.getZ(),en.locX+v.getX(),en.locY+v.getY(),en.locZ+v.getZ());

		}
		List<Entity> list = en.world.getEntities(en, bb);
		if (en instanceof EntityProjectile) {
			list.remove(((EntityProjectile)en).getShooter());
		}
		if (en.getVehicle() != null) list.remove(en.getVehicle());
		list.removeAll(en.passengers);
		list.removeAll(ignore);
		list.remove(en);
		for (Entity entity1 : list) {
			EntityCollideWithEntityEvent ev = new EntityCollideWithEntityEvent(e, entity1.getBukkitEntity(), false, null);
			Bukkit.getPluginManager().callEvent(ev);
			if (ev.isWillCollide()) {
				entity1.collide(en);
			}
		}
	}
}
