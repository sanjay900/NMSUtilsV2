package net.tangentmc.nmsUtils.imagemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.utils.CommandBuilder;

public class ImageMaps implements Listener
{
	private NMSUtils plugin = NMSUtils.getInstance();
	public static final int MAP_WIDTH = 128;
	public static final int MAP_HEIGHT = 128;

	private Map<String, PlacingCacheEntry> placing = new HashMap<String, PlacingCacheEntry>();
	private Map<Short, ImageMap> maps = new HashMap<Short, ImageMap>();
	private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private List<Short> sendList = new ArrayList<Short>();
	private FastSendTask sendTask;


	public ImageMaps()
	{
		if (!new File(plugin.getDataFolder(), "images").exists())
			new File(plugin.getDataFolder(), "images").mkdirs();

		int sendPerTicks = plugin.getConfig().getInt("sendPerTicks", 20);
		int mapsPerSend = plugin.getConfig().getInt("mapsPerSend", 8);

		loadMaps();
		new CommandBuilder("imagemap").withCommandExecutor(new ImageMapCommand(this)).build();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		sendTask = new FastSendTask(this, mapsPerSend);
		plugin.getServer().getPluginManager().registerEvents(sendTask, plugin);
		sendTask.runTaskTimer(plugin, sendPerTicks, sendPerTicks);
	}

	public void onDisable()
	{
		saveMaps();
		Bukkit.getServer().getScheduler().cancelTasks(plugin);
	}

	public List<Short> getFastSendList()
	{
		return sendList;
	}

	public void startPlacing(Player p, String image, boolean fastsend)
	{
		placing.put(p.getName(), new PlacingCacheEntry(image, fastsend));
	}
	public void swapImage(ItemFrame frame, String cache) {
		if (frame == null) return;
		BufferedImage image = loadImage(cache);
		frame.setItem(getMapItem(cache,0,0,image));
		
	}
	public void swapImage(Block b, String cache) {
		ItemFrame frame = b.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1).stream().filter(e->e.getType()==EntityType.ITEM_FRAME).map(e->(ItemFrame)e).findAny().get();
		if (frame == null) {
			Bukkit.getLogger().warning("An invalid itemframe attempted to swap images!");
		}
		BufferedImage image = loadImage(cache);
		frame.setItem(getMapItem(cache,0,0,image));
	}
	public boolean placeImage(Block block, BlockFace face, PlacingCacheEntry cache)
	{
		int xMod = 0;
		int zMod = 0;

		switch (face)
		{
		case EAST:
			zMod = -1;
			break;
		case WEST:
			zMod = 1;
			break;
		case SOUTH:
			xMod = 1;
			break;
		case NORTH:
			xMod = -1;
			break;
		default:
			plugin.getLogger().severe("Someone tried to create an image with an invalid block facing");
			return false;
		}

		BufferedImage image = loadImage(cache.getImage());

		if (image == null)
		{
			plugin.getLogger().severe("Someone tried to create an image with an invalid file!");
			return false;
		}

		Block b = block.getRelative(face);

		int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH);
		int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT);

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if (!block.getRelative(x * xMod, -y, x * zMod).getType().isSolid())
					return false;

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				setItemFrame(b.getRelative(x * xMod, -y, x * zMod), image, face, x * MAP_WIDTH, y * MAP_HEIGHT, cache);

		return true;
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent e)
	{
		if (!e.hasBlock())
			return;

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (!placing.containsKey(e.getPlayer().getName()))
			return;

		if (!placeImage(e.getClickedBlock(), e.getBlockFace(), placing.get(e.getPlayer().getName())))
			e.getPlayer().sendMessage("Can't place the image here!");
		else
			saveMaps();

		placing.remove(e.getPlayer().getName());

	}

	private void setItemFrame(Block bb, BufferedImage image, BlockFace face, int x, int y, PlacingCacheEntry cache)
	{
		ItemFrame i;
		i = bb.getWorld().spawn(bb.getLocation(), ItemFrame.class);
		i.setFacingDirection(face, false);
		ItemStack item = getMapItem(cache.getImage(), x, y, image);
		i.setItem(item);

		short id = item.getDurability();

		if (cache.isFastSend() && !sendList.contains(id))
		{
			sendList.add(id);
			sendTask.addToQueue(id);
		}

		maps.put(id, new ImageMap(cache.getImage(), x, y, sendList.contains(id)));
	}

	@SuppressWarnings("deprecation")
	private ItemStack getMapItem(String file, int x, int y, BufferedImage image)
	{
		ItemStack item = new ItemStack(Material.MAP);

		for (Entry<Short, ImageMap> entry : maps.entrySet())
			if (entry.getValue().isSimilar(file, x, y))
			{
				item.setDurability(entry.getKey());
				return item;
			}

		MapView map = plugin.getServer().createMap(plugin.getServer().getWorlds().get(0));
		for (MapRenderer r : map.getRenderers())
			map.removeRenderer(r);

		map.addRenderer(new ImageMapRenderer(image, x, y));

		item.setDurability(map.getId());

		return item;
	}

	private BufferedImage loadImage(String file)
	{
		if (images.containsKey(file))
			return images.get(file);
		BufferedImage image = null;
		try {
			URL url = new URL(file);
			image = ImageIO.read(url.openStream());
			images.put(file, image);
		} catch (MalformedURLException e1) {
			File f = new File(plugin.getDataFolder(), "images" + File.separatorChar + file);

			if (!f.exists())
				return null;

			try
			{
				image = ImageIO.read(f);
				images.put(file, image);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}

	@SuppressWarnings("deprecation")
	private void loadMaps()
	{
		File file = new File(plugin.getDataFolder(), "maps.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		for (String key : config.getKeys(false))
		{
			short id = Short.parseShort(key);

			MapView map = plugin.getServer().getMap(id);

			for (MapRenderer r : map.getRenderers())
				map.removeRenderer(r);

			String image = config.getString(key + ".image");
			int x = config.getInt(key + ".x");
			int y = config.getInt(key + ".y");
			boolean fastsend = config.getBoolean(key + ".fastsend", false);

			BufferedImage bimage = loadImage(image);

			if (bimage == null)
			{
				plugin.getLogger().warning("Image file " + image + " not found, removing this map!");
				continue;
			}

			if (fastsend)
				sendList.add(id);

			map.addRenderer(new ImageMapRenderer(loadImage(image), x, y));
			maps.put(id, new ImageMap(image, x, y, fastsend));
		}
	}

	private void saveMaps()
	{
		File file = new File(plugin.getDataFolder(), "maps.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		for (String key : config.getKeys(false))
			config.set(key, null);

		for (Entry<Short, ImageMap> e : maps.entrySet())
		{
			config.set(e.getKey() + ".image", e.getValue().getImage());
			config.set(e.getKey() + ".x", e.getValue().getX());
			config.set(e.getKey() + ".y", e.getValue().getY());
			config.set(e.getKey() + ".fastsend", e.getValue().isFastSend());
		}

		try
		{
			config.save(file);
		}
		catch (IOException e1)
		{
			plugin.getLogger().severe("Failed to save maps.yml!");
			e1.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void reloadImage(String file)
	{
		images.remove(file);
		BufferedImage image = loadImage(file);

		int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH);
		int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT);

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				short id = getMapItem(file, x * MAP_WIDTH, y * MAP_HEIGHT, image).getDurability();
				MapView map = plugin.getServer().getMap(id);

				for (MapRenderer renderer : map.getRenderers())
					if (renderer instanceof ImageMapRenderer)
						((ImageMapRenderer) renderer).recalculateInput(image, x * MAP_WIDTH, y * MAP_HEIGHT);

				sendTask.addToQueue(id);
			}

	}
}
