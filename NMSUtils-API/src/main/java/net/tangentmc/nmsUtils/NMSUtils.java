package net.tangentmc.nmsUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import lombok.Setter;
import net.tangentmc.nmsUtils.entities.HologramFactory;
import net.tangentmc.nmsUtils.events.EventListener;
import net.tangentmc.nmsUtils.imagemap.ImageMaps;
import net.tangentmc.nmsUtils.utils.CommandBuilder;
import net.tangentmc.nmsUtils.utils.ReflectionManager;
@Getter
public class NMSUtils extends JavaPlugin implements CommandExecutor, Listener{
	@Getter
	private static NMSUtils instance;
	@Setter
	private NMSUtil util;
	private ImageMaps map;
	private EventListener listener;
	@Override
	public void onEnable() {
		instance = this;
		if (!ReflectionManager.load()) {
			Bukkit.getPluginManager().disablePlugin(this);
		}
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getWorlds().forEach(util::trackWorldEntities);
		new CommandBuilder("spawnlaser").withCommandExecutor(this).build();
		listener= new EventListener();
		map = new ImageMaps();

	}
	@Override
	public void onDisable() {
		Bukkit.getWorlds().forEach(util::untrackWorldEntities);
	}
	//NMSHologram hologram;
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equals("spawnlaser")) {

			/*
			if (hologram != null) {
				hologram.remove();
			}
			Player pl = ((Player)sender);
			Location loc = pl.getLocation();
			//loc.setPitch(0);
			//loc.setYaw(0);
			hologram = new HologramFactory().withLocation(loc).withHead(new ItemStack(Material.STONE),1).withHead(new ItemStack(Material.STONE),1).build();
			hologram.setFrozen(false);
			pl.sendMessage("Hologram SPAWNED!");
			*/
		}
		return false;

	}
}
