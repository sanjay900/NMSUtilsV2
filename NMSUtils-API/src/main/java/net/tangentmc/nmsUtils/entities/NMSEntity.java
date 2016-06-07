package net.tangentmc.nmsUtils.entities;

import net.tangentmc.nmsUtils.NMSUtils;
import org.bukkit.entity.Entity;

public interface NMSEntity {
    String SAVE_TAG = "saves";
    String COLLIDE_TAG = "collides";
    String FROZEN_TAG = "frozen";
	void setFrozen(boolean b);

    /**
     * Set an entity to respond to collisions.
     * @param b
     */
    void setCollides(boolean b);
    void setWillSave(boolean b);
    void setHasBoundingBox(boolean b);
    void spawn();
    boolean willSave();
    Entity getEntity();
    boolean isFrozen();
    boolean willCollide();
    static NMSEntity wrap(Entity en) {
        return NMSUtils.getInstance().getUtil().getNMSEntity(en);
    }
}
