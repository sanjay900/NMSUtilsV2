package net.tangentmc.nmsUtils.v1_10_R1.entities.basic;

import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.v1_10_R1.NMSUtilImpl;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

public class BasicNMSEntity implements NMSEntity {
    Entity en;

    public BasicNMSEntity(Entity en2) {
        en = en2;
    }

    @Override
    public void setFrozen(boolean b) {
        if (b) {
            en.setMetadata(NMSEntity.FROZEN_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
        } else {
            if (en.hasMetadata(NMSEntity.FROZEN_TAG))
                en.removeMetadata(NMSEntity.FROZEN_TAG, NMSUtils.getInstance());
        }
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

    net.minecraft.server.v1_10_R1.Entity orig = null;

    @Override
    public void setWillSave(boolean b) {
        en.setCustomNameVisible(false);
        en.setCustomName("deleteme");
    }

    @Override
    public void setHasBoundingBox(boolean b) {
        ((CraftEntity) en).getHandle().a(new NullBoundingBox());
    }

    @Override
    public void spawn() {
        if (!this.willSave()) {
            NMSUtilImpl.addEntityToWorld(((CraftEntity) en).getHandle().world, ((CraftEntity) en).getHandle());
        }
    }

    @Override
    public boolean willSave() {
        return en.hasMetadata(NMSEntity.SAVE_TAG);
    }

    @Override
    public boolean isFrozen() {
        return en.hasMetadata(NMSEntity.FROZEN_TAG);
    }

    @Override
    public boolean willCollide() {
        return en.hasMetadata(NMSEntity.COLLIDE_TAG);
    }

    @Override
    public Entity getEntity() {
        return en;
    }
}
