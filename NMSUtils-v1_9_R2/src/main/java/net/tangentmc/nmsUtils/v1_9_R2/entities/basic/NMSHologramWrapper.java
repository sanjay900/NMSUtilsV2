package net.tangentmc.nmsUtils.v1_9_R2.entities.basic;

import net.tangentmc.nmsUtils.entities.HologramFactory;
import net.tangentmc.nmsUtils.entities.NMSHologram;
import net.tangentmc.nmsUtils.v1_9_R2.entities.CraftHologramEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by sanjay on 2/06/16.
 */
public class NMSHologramWrapper extends BasicNMSEntity implements NMSHologram {
    public NMSHologramWrapper(Entity en2) {
        super(en2);
    }

    @Override
    public List<Entity> getLines() {
        return ((CraftHologramEntity) en).getLines();
    }

    @Override
    public void setLines(HologramFactory.HologramObject... lines) {
        ((CraftHologramEntity) en).setLines(lines);
    }

    @Override
    public void setLine(int i, String line) {
        ((CraftHologramEntity) en).setLine(i, line);
    }

    @Override
    public void addLine(String line) {
        ((CraftHologramEntity) en).addLine(line);
    }

    @Override
    public void addItem(ItemStack stack) {
        ((CraftHologramEntity) en).addItem(stack);
    }

    @Override
    public void addBlock(ItemStack stack) {
        ((CraftHologramEntity) en).addBlock(stack);
    }

    @Override
    public void removeLine(int idx) {
        ((CraftHologramEntity) en).removeLine(idx);
    }

    @Override
    public void remove() {
        en.remove();
    }
}
