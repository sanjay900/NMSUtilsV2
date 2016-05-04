package net.tangentmc.nmsUtils.entities;

public interface NMSEntity {
    String SAVE_TAG = "saves";
    String COLLIDE_TAG = "collides";
    String FROZEN_TAG = "frozen";
	void setFrozen(boolean b);
    void setCollides(boolean b);
    void setSaves(boolean b);
    void spawn();

    boolean willSave();
    boolean isFrozen();
    boolean willCollide();
}
