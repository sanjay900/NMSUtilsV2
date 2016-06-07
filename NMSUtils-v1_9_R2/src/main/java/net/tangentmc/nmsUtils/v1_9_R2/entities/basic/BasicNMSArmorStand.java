package net.tangentmc.nmsUtils.v1_9_R2.entities.basic;

import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.tangentmc.nmsUtils.entities.NMSArmorStand;
import net.tangentmc.nmsUtils.v1_9_R2.NMSUtilImpl;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

/**
 * Created by sanjay on 2/06/16.
 */
public class BasicNMSArmorStand extends BasicNMSEntity implements NMSArmorStand {
    public BasicNMSArmorStand(Entity en2) {
        super(en2);
    }
    public ArmorStand getEntity() {
        return (ArmorStand) en;
    }
    public void lock() {
        NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity) en).getHandle());
        tag.setInt("DisabledSlots", 2039583);
        ((EntityArmorStand) ((CraftEntity) en).getHandle()).a(tag);
    }

    public void unlock() {
        NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity) en).getHandle());
        tag.setInt("DisabledSlots", 0);
        ((EntityArmorStand) ((CraftEntity) en).getHandle()).a(tag);
    }
}
