package net.tangentmc.nmsUtils;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.tangentmc.nmsUtils.entities.HologramFactory;
import net.tangentmc.nmsUtils.entities.NMSHologram;
import net.tangentmc.nmsUtils.events.EventListener;
import net.tangentmc.nmsUtils.resourcepacks.ResourcePackAPI;
import net.tangentmc.nmsUtils.utils.CommandBuilder;
import net.tangentmc.nmsUtils.utils.MetadataManager;
import net.tangentmc.nmsUtils.utils.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

@Getter
public class NMSUtils extends JavaPlugin implements CommandExecutor, Listener{
	@Getter
	private static NMSUtils instance;
	@Setter
	private NMSUtil util;
	private EventListener listener;
	@Getter
	private ResourcePackAPI resourcePackAPI;
	@Getter
    private MetadataManager metadataManager;
	@Override
	public void onEnable() {
		instance = this;
		createConfig();
		if (!ReflectionManager.load()) {
			Bukkit.getPluginManager().disablePlugin(this);
		}
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getWorlds().forEach(util::trackWorldEntities);
        listener= new EventListener();
        resourcePackAPI = new ResourcePackAPI();
		new CommandBuilder("spawnlaser").withCommandExecutor(this).build();
		resourcePackAPI.registerCommands();
		metadataManager = new MetadataManager();

	}
	@Override
	public void onDisable() {
		Bukkit.getWorlds().forEach(util::untrackWorldEntities);
	}
	NMSHologram hologram;
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
