package net.tangentmc.nmsUtils.v1_9_R1.entities.effects;

import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityProjectile;
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
            list.remove(((EntityProjectile) en).getShooter());
        }
        if (en.getVehicle() != null) list.remove(en.getVehicle());
        list.removeAll(en.passengers);
        list.removeAll(ignore);
        for (int i = 0; i < list.size(); ++i) {
            Entity entity1 = list.get(i);
            EntityCollideWithEntityEvent ev = new EntityCollideWithEntityEvent(e, entity1.getBukkitEntity(), false, null);
            Bukkit.getPluginManager().callEvent(ev);
            if (ev.isWillCollide()) {
                entity1.collide(en);
            }
        }
    }
}
