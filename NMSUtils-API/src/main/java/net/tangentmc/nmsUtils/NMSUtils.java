package net.tangentmc.nmsUtils;

import lombok.Getter;
import lombok.Setter;
import net.tangentmc.nmsUtils.entities.HologramFactory;
import net.tangentmc.nmsUtils.entities.NMSHologram;
import net.tangentmc.nmsUtils.events.EventListener;
import net.tangentmc.nmsUtils.resourcepacks.ResourcePackAPI;
import net.tangentmc.nmsUtils.utils.CommandBuilder;
import net.tangentmc.nmsUtils.utils.ReflectionManager;
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

import java.io.File;

@Getter
public class NMSUtils extends JavaPlugin implements CommandExecutor, Listener{
	@Getter
	private static NMSUtils instance;
	@Setter
	private NMSUtil util;
	private EventListener listener;
	@Getter
	private ResourcePackAPI resourcePackAPI;
	@Override
	public void onEnable() {
		instance = this;
		createConfig();
		if (!ReflectionManager.load()) {
			Bukkit.getPluginManager().disablePlugin(this);
		}
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getWorlds().forEach(util::trackWorldEntities);
		new CommandBuilder("spawnlaser").withCommandExecutor(this).build();
		new CommandBuilder("getItem").withCommandExecutor(this).build();
		new CommandBuilder("getShield").withCommandExecutor(this).build();
		new CommandBuilder("getBow").withCommandExecutor(this).build();
		new CommandBuilder("getWeapon").withCommandExecutor(this).build();
        new CommandBuilder("uploadZip").withCommandExecutor(this).build();
		new CommandBuilder("setBlock").withCommandExecutor(this).build();
		new CommandBuilder("updateItem").withCommandExecutor(this).build();
		listener= new EventListener();
		resourcePackAPI = new ResourcePackAPI();

	}
	@Override
	public void onDisable() {
		Bukkit.getWorlds().forEach(util::untrackWorldEntities);
	}
	NMSHologram hologram;
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	    if (label.equals("uploadzip")) {
            Bukkit.getScheduler().runTaskAsynchronously(this,()->{
				try {
					resourcePackAPI.uploadZIP();
					resourcePackAPI.updatePacks();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
        }
        if (label.equals("setblock")) {
			if (sender instanceof Player) {
				resourcePackAPI.setBlock(((Player) sender).getLocation(),args[0]);
			}
		}
		if (label.equals("getitem")) {
			if (sender instanceof Player) {
				((Player) sender).getInventory().addItem(resourcePackAPI.getItemStack(args[0]));
			}
		}
		if (label.equals("getbow")) {
			if (sender instanceof Player) {
				((Player) sender).getInventory().addItem(resourcePackAPI.getBow(args[0]));
			}
		}
		if (label.equals("getshield")) {
			if (sender instanceof Player) {
				((Player) sender).getInventory().addItem(resourcePackAPI.getShield(args[0]));
			}
		}
		if (label.equals("getweapon")) {
			if (sender instanceof Player) {
				((Player) sender).getInventory().addItem(resourcePackAPI.getWeapon(args[0]));
			}
		}
		if (label.equals("updateitem")) {
	    	String[] args2 = new String[args.length-1];
	    	System.arraycopy(args,1,args2,0,args2.length);
			resourcePackAPI.getModelInfo(args[0]).updateViaCommand(args2, sender);
		}
		if (label.equals("spawnlaser")) {
			if (hologram != null) {
				hologram.remove();
			}
			Player pl = ((Player)sender);
			Location loc = pl.getLocation();
			//loc.setPitch(0);
			//loc.setYaw(0);
			hologram = new HologramFactory().withLocation(loc).withHead(new ItemStack(Material.STONE),1).withBlock(new ItemStack(Material.STONE)).withLine("TEST").withItem(new ItemStack(Material.ACACIA_DOOR)).build();
			pl.sendMessage("Hologram SPAWNED!");

		}
		return false;

	}
	private void createConfig() {
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}
			File file = new File(getDataFolder(), "config.yml");
			if (!file.exists()) {
				getLogger().info("Config.yml not found, creating!");
				saveDefaultConfig();
			} else {
				getLogger().info("Config.yml found, loading!");
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}
}
