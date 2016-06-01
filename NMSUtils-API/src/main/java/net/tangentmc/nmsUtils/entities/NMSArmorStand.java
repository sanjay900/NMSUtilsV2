package net.tangentmc.nmsUtils.entities;

import net.tangentmc.nmsUtils.NMSUtils;
import org.bukkit.entity.ArmorStand;

public interface NMSArmorStand extends NMSEntity{
	void lock();
	void unlock();
	ArmorStand getEntity();
	static NMSArmorStand wrap(ArmorStand as) {
		return (NMSArmorStand) NMSUtils.getInstance().getUtil().getNMSEntity(as);
	}
}
