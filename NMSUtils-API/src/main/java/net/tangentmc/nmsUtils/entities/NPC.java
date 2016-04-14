package net.tangentmc.nmsUtils.entities;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface NPC extends NMSEntity {

	void logout(Player pl);

	void spawn(Player player);

	boolean teleport(Location to);

}
