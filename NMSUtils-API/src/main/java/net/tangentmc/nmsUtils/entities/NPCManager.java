package net.tangentmc.nmsUtils.entities;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.tangentmc.nmsUtils.NMSUtils;

public class NPCManager implements Listener {
	private Set<NPC> npcs = Collections.newSetFromMap(new WeakHashMap<>());
	public NPCManager() {
		Bukkit.getPluginManager().registerEvents(this, NMSUtils.getInstance());
	}
	public void addNPC(NPC npc) {
		npcs.add(npc);
	}
	public void removeNPC(NPC npc) {
		npcs.remove(npc);
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent evt) {
		npcs.forEach(npc->npc.logout(evt.getPlayer()));
	}
	@EventHandler
	public void onLogin(PlayerJoinEvent evt) {
		Bukkit.getScheduler().runTask(NMSUtils.getInstance(), () -> {
			npcs.forEach(npc->npc.spawn(evt.getPlayer()));
		});
	}
}
