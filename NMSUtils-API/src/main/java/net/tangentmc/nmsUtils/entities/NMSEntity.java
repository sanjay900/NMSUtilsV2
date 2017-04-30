package net.tangentmc.nmsUtils.entities;

import net.tangentmc.nmsUtils.NMSUtils;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.List;

public interface NMSEntity {
    String WONT_SAVE_TAG = "saves";
    String COLLIDE_TAG = "collides";
    String BOUNDING_TAG = "box";

    /**
     * Set an entity to respond to collisions.
     * @param b
     */
    default void setCollides(boolean b) {
        if (b) addEntityTag(NMSEntity.COLLIDE_TAG);
        else removeEntityTag(NMSEntity.COLLIDE_TAG);
    }
    void setHasBoundingBox(boolean b);
    Entity getEntity();


    default void setWillSave(boolean b) {
        if (b) addEntityTag(NMSEntity.WONT_SAVE_TAG);
        else removeEntityTag(NMSEntity.WONT_SAVE_TAG);
    }


    default boolean willSave() {
        return !hasEntityTag(NMSEntity.WONT_SAVE_TAG);
    }


    default boolean willCollide() {
        return hasEntityTag(NMSEntity.COLLIDE_TAG);
    }

    /**
     *
     * @param x distance from center to lowest x
     * @param y distance from center to lowest y
     * @param z distance from center to lowest z
     */
    default void setSize(double x, double y, double z) {
        getEntity().setMetadata(BOUNDING_TAG,new FixedMetadataValue(NMSUtils.getInstance(),new Vector(x,y,z)));
    }

    /**
     * Add a string to an entity that will survive a restart
     * @param text the string to add
     */
    void addEntityTag(String text);

    void removeEntityTag(String text);

    boolean hasEntityTag(String text);
    List<String> getTags();
    static NMSEntity wrap(Entity en) {
        return NMSUtils.getInstance().getUtil().getNMSEntity(en);
    }
}

