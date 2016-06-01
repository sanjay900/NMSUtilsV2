package net.tangentmc.nmsUtils.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.util.Vector;

import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.utils.FaceUtil;

public class EventListener implements Listener {
	NMSUtils util = NMSUtils.getInstance();
	public EventListener() {
		Bukkit.getPluginManager().registerEvents(this, util);
	}
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		if (event.getFrom().distanceSquared(event.getTo())==0) return;
		Player e = event.getPlayer();
		Vector movement = event.getTo().toVector().subtract(event.getFrom().toVector());
		Bukkit.getScheduler().runTask(util, ()-> {
			Block b = event.getTo().getBlock().getRelative(FaceUtil.getDirection(movement));
			if (FaceUtil.isSubCardinal(FaceUtil.getDirection(movement))) {
				BlockFace[] sub = FaceUtil.getFaces(FaceUtil.getDirection(movement));
				if (!event.getTo().getBlock().getRelative(sub[0]).getType().isSolid()&&!event.getTo().getBlock().getRelative(sub[1]).getType().isSolid()) {
					if (b.getType()!= Material.AIR) {
						EntityCollideWithBlockEvent evt = new EntityCollideWithBlockEvent(e, b, movement,FaceUtil.getDirection(movement));
						Bukkit.getPluginManager().callEvent(evt);
						return;
					}
				}
			} else if (b.getType()!= Material.AIR) {
				EntityCollideWithBlockEvent evt = new EntityCollideWithBlockEvent(e, b, movement,FaceUtil.getDirection(movement));
				Bukkit.getPluginManager().callEvent(evt);
				return;
			}
			b = event.getTo().getBlock();
			if (b.getType()!= Material.AIR) {
				EntityCollideWithBlockEvent evt = new EntityCollideWithBlockEvent(e, b, movement,BlockFace.SELF);
				Bukkit.getPluginManager().callEvent(evt);
				return;
			}
			b = event.getTo().getBlock().getRelative(BlockFace.DOWN);
			if (b.getType()!= Material.AIR) {
				EntityCollideWithBlockEvent evt = new EntityCollideWithBlockEvent(e, b, movement.setY(-0.1), BlockFace.DOWN);
				Bukkit.getPluginManager().callEvent(evt);
				return;
			}
		});
	}
	@EventHandler
	public void chunkLoad(ChunkLoadEvent evt) {
		util.getUtil().loadChunk(evt.getChunk());
	}
	@EventHandler
	public void worldLoad(WorldLoadEvent evt) {
		util.getUtil().trackWorldEntities(evt.getWorld());
	}
	@EventHandler
	public void worldUnload(WorldUnloadEvent evt) {
		util.getUtil().untrackWorldEntities(evt.getWorld());
	}
	@EventHandler
	public void collide(EntityCollideWithEntityEvent evt) {
		//System.out.print(evt);
	}
}
