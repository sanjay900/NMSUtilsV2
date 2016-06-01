package net.tangentmc.nmsUtils.entities;

import java.util.List;

import net.tangentmc.nmsUtils.NMSUtils;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import net.tangentmc.nmsUtils.entities.HologramFactory.HologramObject;

public interface NMSHologram extends NMSEntity {
    List<Entity> getLines();
    void setLines(HologramObject... lines);
    void setLine(int i, String line);
    void addLine(String line);
    void addItem(ItemStack stack);
    void addBlock(ItemStack stack);
    void removeLine(int idx);
    void remove();
    static NMSHologram wrap(Entity en) {
        return (NMSHologram) NMSUtils.getInstance().getUtil().getNMSEntity(en);
    }
}