package net.tangentmc.nmsUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
/**
 * A Comparable Location
 * @author V10Lator
 *
 */
public class V10Location {

    protected final double x, y, z;
    protected final String world;
    
    /**
     * Create a V10Location from an existing Location
     * @param location Location to create from
     */
    public V10Location(Location location) {
        this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }
    
    /**
     * Create a V10Location from a world and coords
     * @param world the world as a string
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     */
    public V10Location(String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    /**
     * Create a V10Location from a world and coords
     * @param world the world
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     */
    public V10Location(World world, double x, double y, double z) {
        this(world.getName(), x, y, z);
    }
    /**
     * Create a V10Location from a block
     * @param block
     */
    public V10Location(Block block) {
        this(block.getLocation());
    }
    /**
     * Get a Location based on this V10Location
     * @return a Location based on this V10Location
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
    public double getX() {
        return x;
    }
    /**
     * Get the Y coordinate
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }
    /**
     * Get the Z coordinate
     * @return the z coordinate
     */
    public double getZ() {
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
     * Create a new V10Location from an existing one
     */   
    public V10Location clone() {
        return new V10Location(world, x, y, z);
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
        if(obj != null && obj instanceof V10Location) {
            V10Location other = (V10Location)obj;
            return (world == null && other.world == null) ||
                    (world != null && world.equals(other.world)) &&
                    x ==other.x &&
                    		y == other.y &&
                    				z == other.z;
        }
        if (obj != null &&obj instanceof Block) {
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
}
