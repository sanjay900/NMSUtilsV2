/**
 * Copyright (C) 2015 Mark Hendriks
 * <p>
 * This file is part of DarkSeraphim's NPC library.
 * <p>
 * DarkSeraphim's NPC library is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * <p>
 * DarkSeraphim's NPC library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DarkSeraphim's NPC library. If not, see <http://www.gnu.org/licenses/>.
 */

package net.tangentmc.nmsUtils.v1_9_R1.entities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author DarkSeraphim.
 */
@SuppressWarnings("rawtypes")
public class NPC extends EntityPlayer{
	public static class CraftNPC extends CraftPlayer implements net.tangentmc.nmsUtils.entities.NPC{
		public CraftNPC(NPC entity) {
			super((CraftServer) Bukkit.getServer(), entity);
		}
		@Override
		public boolean teleport(Location loc, TeleportCause cause) {
			entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			return true;
		}

		@Override
		public void logout(Player pl) {
			((NPC)entity).logout(pl);
		}

		@Override
		public void spawn(Player pl) {
			((NPC)entity).spawn(pl);
		}


		@Override
		public void remove() {
			Bukkit.getOnlinePlayers().forEach(this::logout);
		}
		@Override
		public void setFrozen(boolean b) {
			
		}

        @Override
        public void setCollides(boolean b) {

        }

        @Override
        public void setWillSave(boolean b) {

        }

        @Override
		public boolean isFrozen() {
			return false;
		}

        @Override
        public boolean willCollide() {
            return true;
        }
		@Override
		public boolean willSave() {
			return false;
		}

        @Override
        public Entity getEntity() {
            return this;
        }

        @Override
		public void spawn() {
			
		}
		
	}
    // Returns the Packets required to spawn a NPC in form of a List
    private static final Function<NPC, List<Packet>> getSpawnPackets = (npc) -> {
        if (npc.spawnPackets == null) {
            Packet[] packets = new Packet[]{
                    new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, npc),
                    new PacketPlayOutNamedEntitySpawn(npc)
            };
            npc.spawnPackets = Arrays.asList(packets);
        }
        return npc.spawnPackets;
    };

    // Returns the Packet required to despawn a NPC
    private static final Function<NPC, Consumer<PlayerConnection>> getDespawnPacket = (npc) -> {
        if (npc.despawnPacket == null) {
            npc.despawnPacket = new PacketPlayOutEntityDestroy(npc.getId());
        }
        return (connection) -> connection.sendPacket(npc.despawnPacket);
    };

    private final Plugin plugin;

    private List<Packet> spawnPackets;

    private Packet despawnPacket;

    private Set<UUID> tracked = new HashSet<>();

    private BukkitTask task;

    private NPC(Plugin plugin, WorldServer world, GameProfile profile, PlayerInteractManager manager, Location location) {
        super(MinecraftServer.getServer(), world, profile, manager);
        this.plugin = plugin;
        this.ping = 100;
    }

    /**
     * @return the name shown in the tab list
     */
    @Override
    public IChatBaseComponent getPlayerListName() {
        // Override to set a name
        // Not like we would need it, we don't even want a tab list entry
        return null;
    }

    /**
     * Creates an NPC, owned by {@code plugin}, with name {@code name} at {@code location}.
     *
     * @param plugin   Plugin which owns the NPC.
     * @param name     Name of the NPC.
     * @param location Location where the NPC should spawn.
     * @return NPC which was created.
     */
    public static NPC createNPC(Plugin plugin, String name, Location location, String value, String signature) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", new Property("textures", value, signature));
        WorldServer world = getWorld(location.getWorld());
        NPC npc = new NPC(plugin, world, profile, new PlayerInteractManager(world), location);
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        return npc;
    }

    public static void spinHead(NPC npc, byte yaw) {
        Packet packet = new PacketPlayOutEntityHeadRotation(npc, yaw);
        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }
    @Override
    public void setLocation(double x, double y, double z, float yaw, float pitch) {
    	super.setLocation(x, y, z, yaw, pitch);
    	Packet packet = new PacketPlayOutEntityTeleport(this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    // Returns the NMS World from the Bukkit World
    private static WorldServer getWorld(World world) {
        return ((CraftWorld) world).getHandle();
    }

    // Returns the PlayerConnection of the Player
    private PlayerConnection getConnection(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection;
    }

    /**
     * Spawns (shows) this NPC for Player.
     *
     * @param player player to spawn NPC for.
     */
    public void spawn(Player player) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            PlayerConnection connection = getConnection(player);
            NPC.getSpawnPackets.apply(this).stream().forEach(connection::sendPacket);
            this.tracked.add(player.getUniqueId());
            if (this.task == null) {
                this.task = new NPCTrackerTask(this).runTaskTimer(this.plugin, 0L, 5L);
            }

            spinHead(this, (byte) ((yaw * 256.0F) / 360.0F));
        }, 1L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PlayerConnection connection = getConnection(player);
            connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, this));
        }, 20L);

    }

    /**
     * Returns a Stream of all Players which track this NPC.
     *
     * @return a {@code Stream<Player>} of tracked Players
     */
    protected Stream<Player> getTrackedPlayers() {
        return this.tracked.stream().map(Bukkit::getPlayer).filter(player -> player != null);
    }

    /**
     * Despawns (hides) NPC for the given Player.
     *
     * @param player player to despawn NPC for.
     */
    public void despawn(Player player) {
        despawn(player, false);
    }
    /**
     * Despawns (hides) NPC for the given Player.
     *
     * @param player player to despawn NPC for.
     */
    public void logout(Player player) {
        despawn(player, true);
    }
    /**
     * Despawns (hides) NPC for the given Player.
     * <p>
     * <i>Note: always invoke this method onQuit. It's good memory management!</i>
     *
     * @param player player to despawn NPC for.
     * @param logout whether it was triggered by a logout.
     */
    public void despawn(Player player, boolean logout) {
        this.tracked.remove(player.getUniqueId());
        if (this.tracked.isEmpty()) {
            this.task.cancel();
            this.task = null;
        }

        if (!logout) {
            cleanup(player);
        }
    }

    /**
     * Sends a PacketPlayOutDestroyEntity to the Player.
     *
     * @param player Player which will receive the packet
     */
    protected void cleanup(Player player) {
        PlayerConnection connection = getConnection(player);
        NPC.getDespawnPacket.apply(this).accept(connection);
    }
    @Override
    public CraftNPC getBukkitEntity() {
    	return new CraftNPC(this);
    }
    /**
     * Auxiliary method which returns the distance^2 between
     * the Player and this NPC
     *
     * @param player Player with which the distance should be computed for.
     * @return the distance^2 between the Player and this NPC
     */
    protected double getDistanceSquared(Player player) {
        Location loc = player.getLocation();
        double dx = loc.getX() - this.locX;
        double dy = loc.getY() - this.locY;
        double dz = loc.getZ() - this.locZ;
        return dx * dx + dy * dy + dz * dz;
    }
    @Override
    public void m() {
    	Collideable.testCollision(this);
    	super.m();
    }

}