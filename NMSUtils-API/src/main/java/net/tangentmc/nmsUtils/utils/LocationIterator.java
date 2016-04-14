package net.tangentmc.nmsUtils.utils;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
/**
 * Get a Iterated List of blocks from a location in a specific direction
 * @author Sanjay
 *
 */
public class LocationIterator implements Iterator<Location>{
    private int maxDistanceInt;
    private int currentLocation = 0;
    private Location curLocation;
    private Vector direction;
    public LocationIterator(World world, Vector start, Vector direction,  int maxDistance) {
        maxDistanceInt = maxDistance;
        Vector startClone = start.clone();
        this.direction = direction;
        curLocation = startClone.toLocation(world);
    }

    public LocationIterator(Location loc, int maxDistance) {
        this(loc.getWorld(), loc.toVector(), loc.getDirection(), maxDistance);
    }
 
    public LocationIterator(LivingEntity entity, int maxDistance) {
        this(entity.getLocation(),maxDistance);
    }
   
    public boolean hasNext() {
        return currentLocation + 1 <= maxDistanceInt;
    }
 
    public Location next() {
        scan();
        return this.curLocation;
    }
 
    public void remove() {
        throw new UnsupportedOperationException("[LocationIterator] doesn't support location removal");
    }
 
    private void scan() {
    	currentLocation++;
        this.curLocation.add(direction).setDirection(direction);
    }
}