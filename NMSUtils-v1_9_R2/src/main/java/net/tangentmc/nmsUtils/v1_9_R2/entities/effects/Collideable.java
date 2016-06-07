package net.tangentmc.nmsUtils.v1_9_R2.entities.effects;

import net.minecraft.server.v1_9_R2.AxisAlignedBB;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityProjectile;
import net.tangentmc.nmsUtils.events.EntityCollideWithEntityEvent;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class Collideable {
    public static void testCollision(Entity en) {
        testCollision(en, new ArrayList<>());
    }
	public static void testCollision(Entity en, List<Entity> ignore) {
		org.bukkit.entity.Entity e = en.getBukkitEntity();
		AxisAlignedBB bb = en.getBoundingBox();
		List<Entity> list = en.world.getEntities(en, bb);
		if (en instanceof EntityProjectile) {
			list.remove(((EntityProjectile)en).getShooter());
		}
		if (en.getVehicle() != null) list.remove(en.getVehicle());
		list.removeAll(en.passengers);
		list.removeAll(ignore);
		list.remove(en);
		for (Entity entity1 : list) {
			EntityCollideWithEntityEvent ev = new EntityCollideWithEntityEvent(e, entity1.getBukkitEntity(), false);
			Bukkit.getPluginManager().callEvent(ev);
			if (ev.isWillCollide()) {
				entity1.collide(en);
			}
		}
	}
}
