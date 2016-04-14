package net.tangentmc.nmsUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
/**
 * A Comparable Location based around blocks
 * @author V10Lator
 *
 */
public class V10Block {

    private final int x, y, z;
    private final String world;
    /**
     * Create a V10BlockLocation from an existing Location
     * @param location the Location to create from
     */
    public V10Block(Location location) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    /**
     * Create a v10BlockLocation from a world and coords
     * @param world the world as a string
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     */
    public V10Block(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    /**
     * Create a v10BlockLocation from a world and coords
     * @param world the world
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     */
    public V10Block(World world, int x, int y, int z) {
        this(world.getName(), x, y, z);
    }
    /**
     * Create a V10Location from a block
     * @param block the block to create the V10blockLocation from
     */
    public V10Block(Block block) {
        this(block.getLocation());
    }
    /**
     * Create a Location based around this V10BlockLocation
     * @return
     */
    public Location getHandle() {
        World world = Bukkit.getWorld(this.world);
        if(world == null)
            return null;
        return new Location(world, x, y, z);
    }
    /**
     * Get the X coordinate
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }
    /**
     * Get the Y coordinate
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }
    /**
     * Get the Z coordinate
     * @return the z coordinate
     */
    public int getZ() {
        return z;
    }
    /**
     * Get the World name
     * @return the world name as a string
     */    
    public String getWorldName() {
        return world;
    }
    /**
     * Create a new V10BlockLocation from an existing one
     */
    public V10Block clone() {
        return new V10Block(world, x, y, z);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((world == null) ? 0 : world.hashCode());
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj != null && obj instanceof V10Block) {
            V10Block other = (V10Block)obj;
            return (world == null && other.world == null) ||
                    (world != null && world.equals(other.world)) &&
                    x ==other.x &&
                    		y == other.y &&
                    				z == other.z;
        }
        if (obj != null && obj instanceof Block) {
        	Block other = (Block)obj;
            return (world == null && other.getWorld() == null) ||
                    (world != null && world.equals(other.getWorld())) &&
                    x ==other.getX() &&
                    		y == other.getY() &&
                    				z == other.getZ();
        }
        if (obj != null &&obj instanceof Location) {
        	Location other = (Location)obj;
            return (world == null && other.getWorld() == null) ||
                    (world != null && world.equals(other.getWorld())) &&
                    x ==other.getX() &&
                    		y == other.getY() &&
                    				z == other.getZ();
        }
        return false;
    }
    @Override
    public String toString() {
    	return (this.world+","+this.x+","+this.y+","+this.z);
    }
	public V10Block getRelative(BlockFace face) {
		return new V10Block(world, x+face.getModX(), y+face.getModY(), z+face.getModZ());
	}
}
