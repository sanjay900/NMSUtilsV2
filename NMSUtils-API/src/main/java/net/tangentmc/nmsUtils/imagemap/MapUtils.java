package net.tangentmc.nmsUtils.imagemap;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MapUtils implements Listener {

   private Plugin plugin;
   private List frames = new ArrayList();
   private List renderers = new ArrayList();


   public MapUtils(Plugin plugin) {
      this.plugin = plugin;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   public void refreshImages() {
      Iterator var1 = Bukkit.getOnlinePlayers().iterator();

      while(var1.hasNext()) {
         Player player = (Player)var1.next();
         this.refreshImages(player);
      }

   }

   public void refreshImages(Player player) {
      Iterator var2 = this.frames.iterator();

      while(var2.hasNext()) {
         ItemFrame frame = (ItemFrame)var2.next();
         MapView map = Bukkit.getMap(frame.getItem().getDurability());
         player.sendMap(map);
      }

   }

   public Image getImageFromURL(String url) {
      try {
         return ImageIO.read(new URL(url));
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public void makeArt(Location location, Image image) {
      ItemFrame frame = this.getFrame(location);
      if(frame == null) {
         throw new IllegalStateException("Unable to retrieve item frame. Wrong coordinates?");
      } else {
         MapView map = Bukkit.createMap(location.getWorld());
         Iterator var5 = map.getRenderers().iterator();

         while(var5.hasNext()) {
            MapRenderer render = (MapRenderer)var5.next();
            map.removeRenderer(render);
         }

         map.addRenderer(new CustomMapRenderer(this, image));
         frame.setItem(new ItemStack(Material.MAP, 1, map.getId()));
         this.frames.add(frame);
      }
   }

   private ItemFrame getFrame(Location loc) {
      Entity[] var2 = loc.getChunk().getEntities();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Entity entity = var2[var4];
         if(entity instanceof ItemFrame && entity.getLocation().getBlock().getLocation().distance(loc) == 0.0D) {
            return (ItemFrame)entity;
         }
      }

      return null;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      new BukkitRunnable(){
		@Override
		public void run() {
			MapUtils.this.refreshImages(player);
		}}.runTaskLater(this.plugin, 30L);
   }

   @EventHandler
   public void onBreak(HangingBreakEvent event) {
      if(this.frames.contains(event.getEntity())) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID uuid = event.getPlayer().getUniqueId();
      Iterator var3 = this.renderers.iterator();
      while(var3.hasNext()) {
         CustomMapRenderer rendered = (CustomMapRenderer)var3.next();
         rendered.getRendered().remove(uuid);
      }

   }
}