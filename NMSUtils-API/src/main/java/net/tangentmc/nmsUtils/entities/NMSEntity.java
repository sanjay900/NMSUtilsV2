package net.tangentmc.nmsUtils.entities;

public interface NMSEntity {
	void remove();
	boolean isValid();
	void setFrozen(boolean b);
	boolean isFrozen();
	void setWillSave(boolean b);
	boolean willSave();
	void spawn();
}
