package net.tangentmc.nmsUtils.v1_10_R1.entities.basic;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_10_R1.*;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.v1_10_R1.NMSUtilImpl;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public class BasicNMSEntity implements NMSEntity {
    Entity en;

    public BasicNMSEntity(Entity en2) {
        en = en2;
    }

    @Override
    public void setCollides(boolean b) {
        if (b) {
            en.setMetadata(NMSEntity.COLLIDE_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
        } else {
            if (en.hasMetadata(NMSEntity.COLLIDE_TAG))
                en.removeMetadata(NMSEntity.COLLIDE_TAG, NMSUtils.getInstance());
        }
    }

    @Override
    public void setWillSave(boolean b) {
        addEntityTag(NMSEntity.WONT_SAVE_TAG);
    }
    @Override
    public void setHasBoundingBox(boolean b) {
        ((CraftEntity) en).getHandle().a(new NullBoundingBox());
    }
    @Override
    public boolean willSave() {
        return !hasEntityTag(NMSEntity.WONT_SAVE_TAG);
    }

    @Override
    public boolean willCollide() {
        return en.hasMetadata(NMSEntity.COLLIDE_TAG);
    }

    @Override
    public void addEntityTag(String text) {
        NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity) en).getHandle());
        NBTTagString stag = new NBTTagString(text);
        NBTTagList list = tag.getList("Tags", stag.getTypeId());
        list.add(stag);
        tag.set("Tags",list);
        ((EntityArmorStand) ((CraftEntity) en).getHandle()).a(tag);
    }

    @Override
    public boolean hasEntityTag(String text) {
        NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity) en).getHandle());
        NBTTagList list = tag.getList("Tags", new NBTTagString("").getTypeId());
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(text)) return true;
        }
        return false;
    }

    @Override
    public List<String> getTags() {
        NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity) en).getHandle());
        List<String> tags = Lists.newArrayList();
        NBTTagList list = tag.getList("Tags", new NBTTagString("").getTypeId());
        for (int i = 0; i < list.size(); i++) {
            tags.add(list.getString(i));
        }
        return tags;
    }

    @Override
    public Entity getEntity() {
        return en;
    }
}
