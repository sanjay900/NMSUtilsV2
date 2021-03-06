package net.tangentmc.nmsUtils.events;

import net.tangentmc.nmsUtils.NMSUtil;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.resourcepacks.InvalidItemException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.utils.FaceUtil;

import java.util.List;

public class EventListener implements Listener {
	NMSUtils util = NMSUtils.getInstance();
	public EventListener() {
		Bukkit.getPluginManager().registerEvents(this, util);
	}
	@EventHandler
	public void blockDamage(BlockDamageEvent evt) {
		try {
			String item = util.getResourcePackAPI().getItemStack(NMSUtils.getInstance().getUtil().getStackFromSpawner(evt.getBlock()));
			evt.setInstaBreak(util.getResourcePackAPI().getModelInfo(item).isBreakImmediately());
		} catch (InvalidItemException ex) {}
	}
	@EventHandler
	public void resourcePack(PlayerResourcePackStatusEvent evt) {
		if (evt.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
			evt.getPlayer().sendMessage("You have rejected the server resource pack! A lot of features depend upon this pack, it is recommended that you enable it.");
		}
	}
	@EventHandler(ignoreCancelled = true,priority= EventPriority.HIGH)
	public void blockBreak(BlockBreakEvent evt) {
		if (evt.getBlock().getType() == Material.MOB_SPAWNER && evt.getPlayer().getGameMode() != GameMode.CREATIVE) {
			try {
				ItemStack stack = NMSUtils.getInstance().getUtil().getStackFromSpawner(evt.getBlock());
				String item = util.getResourcePackAPI().getItemStack(stack);
				evt.getBlock().setType(Material.AIR);
				evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(),stack);
				evt.setCancelled(true);
			} catch (InvalidItemException ex) {}
		}
	}
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		entityMove(event.getPlayer(),event.getFrom(),event.getTo());
	}
	@EventHandler
	public void entityMove(EntityMoveEvent event) {
		entityMove(event.getEntity(),event.getFrom(),event.getTo());
	}
	private void entityMove(Entity e, Location from, Location to) {
		if (from.distanceSquared(to)==0) return;
		Vector movement = to.toVector().subtract(from.toVector());
		Bukkit.getScheduler().runTask(util, ()-> {
			Block b = to.getBlock().getRelative(FaceUtil.getDirection(movement));
			if (FaceUtil.isSubCardinal(FaceUtil.getDirection(movement))) {
				BlockFace[] sub = FaceUtil.getFaces(FaceUtil.getDirection(movement));
				if (!to.getBlock().getRelative(sub[0]).getType().isSolid()&&!to.getBlock().getRelative(sub[1]).getType().isSolid()) {
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
			b = to.getBlock();
			if (b.getType()!= Material.AIR) {
				EntityCollideWithBlockEvent evt = new EntityCollideWithBlockEvent(e, b, movement,BlockFace.SELF);
				Bukkit.getPluginManager().callEvent(evt);
				return;
			}
			b = to.getBlock().getRelative(BlockFace.DOWN);
			if (b.getType()!= Material.AIR) {
				EntityCollideWithBlockEvent evt = new EntityCollideWithBlockEvent(e, b, movement.setY(-0.1), BlockFace.DOWN);
				Bukkit.getPluginManager().callEvent(evt);
			}
		});

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
	public void playerJoin(PlayerJoinEvent evt) {
		Bukkit.getScheduler().runTaskLater(NMSUtils.getInstance(),()->NMSUtils.getInstance().getResourcePackAPI().updatePacks(evt.getPlayer()),1L);
	}
	@EventHandler
	public void blockPlace(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		//Always check main hand, as that is the hand you place blocks from.
		String name = NMSUtils.getInstance().getResourcePackAPI().getItemStack(event.getPlayer().getInventory().getItemInMainHand());
		if (name != null && name.startsWith("block")) {
			event.setCancelled(true);
			NMSUtils.getInstance().getResourcePackAPI().setBlock(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),name);
		}
	}

}
