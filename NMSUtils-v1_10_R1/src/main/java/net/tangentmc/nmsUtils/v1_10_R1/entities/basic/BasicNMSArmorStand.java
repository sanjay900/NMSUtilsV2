package net.tangentmc.nmsUtils.v1_10_R1.entities.basic;

import net.minecraft.server.v1_10_R1.EntityArmorStand;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.tangentmc.nmsUtils.entities.NMSArmorStand;
import net.tangentmc.nmsUtils.v1_10_R1.NMSUtilImpl;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
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
        tag.setInt("DisablaedSlots", 2039583);
        ((EntityArmorStand) ((CraftEntity) en).getHandle()).a(tag);
    }

    public void unlock() {
        NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity) en).getHandle());
        tag.setInt("DisabledSlots", 0);
        ((EntityArmorStand) ((CraftEntity) en).getHandle()).a(tag);
    }
}
