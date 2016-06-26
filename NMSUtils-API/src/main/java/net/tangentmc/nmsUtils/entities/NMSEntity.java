package net.tangentmc.nmsUtils.entities;

import net.tangentmc.nmsUtils.NMSUtils;
import org.bukkit.entity.Entity;

import java.util.List;

public interface NMSEntity {
    String WONT_SAVE_TAG = "saves";
    String COLLIDE_TAG = "collides";

    /**
     * Set an entity to respond to collisions.
     * @param b
     */
    void setCollides(boolean b);
    void setWillSave(boolean b);
    void setHasBoundingBox(boolean b);
    boolean willSave();
    Entity getEntity();
    boolean willCollide();

    /**
     * Add a string to an entity that will survive a restart
     * @param text the string to add
     */
    void addEntityTag(String text);
    boolean hasEntityTag(String text);
    List<String> getTags();
    static NMSEntity wrap(Entity en) {
        return NMSUtils.getInstance().getUtil().getNMSEntity(en);
    }
}
