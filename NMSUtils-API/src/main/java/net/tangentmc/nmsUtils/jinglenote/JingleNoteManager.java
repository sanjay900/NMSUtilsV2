package net.tangentmc.nmsUtils.jinglenote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import net.tangentmc.nmsUtils.NMSUtils;



/**
 * A manager of play instances.
 *
 * @author sk89q
 */
public class JingleNoteManager {
	public JingleNoteManager() {
		Bukkit.getScheduler().runTaskTimer(NMSUtils.getInstance(), ()->{
			Iterator<JingleNotePlayer> it = playingNearby.keySet().iterator();
			while (it.hasNext()) {
				if (!it.next().isPlaying())
					it.remove();
			}
			it = instances.values().iterator();
			while (it.hasNext()) {
				if (!it.next().isPlaying())
					it.remove();
			}
			for (Player pl: Bukkit.getOnlinePlayers()) {
				if (instances.containsKey(pl.getName())) {
					if (pl.getLocation().distanceSquared(playingNearby.get(instances.get(pl.getName())))>instances.get(pl.getName()).area)
						instances.get(pl.getName()).removePlayer(pl);
						continue;
				}

				for (Entry<JingleNotePlayer, Location> to:playingNearby.entrySet()) {
					if (pl.getLocation().getWorld() == to.getValue().getWorld()) {
						if (pl.getLocation().distanceSquared(to.getValue())<to.getKey().area) {
							to.getKey().playTo(pl);
						}
					}
				}
			}
		}, 1l, 1l);
	}
	/**
	 * List of instances.
	 */
	protected final Map<String, JingleNotePlayer> instances = new HashMap<String, JingleNotePlayer>();
	protected final Map<JingleNotePlayer, Location> playingNearby = new WeakHashMap<JingleNotePlayer, Location>();
	public boolean isPlaying(String player) {

		return instances.containsKey(player) && instances.get(player).isPlaying();
	}

	public boolean isPlaying() {

		/*if(instances.isEmpty()) return false;
        Iterator<String> iter = instances.keySet().iterator();
        while(iter.hasNext()) {
            String ent = iter.next();
            if(!isPlaying(ent))
                stop(ent);
        }*/
		return !instances.isEmpty();
	}

	public void play(String player, JingleSequencer sequencer) {

		// Existing player found!
		if (instances.containsKey(player)) {
			stop(player);
		}

		JingleNotePlayer notePlayer = new JingleNotePlayer(sequencer,20,player);
		Thread thread = new Thread(notePlayer);
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setName("JingleNotePlayer for " + player);
		thread.start();

		instances.put(player, notePlayer);
	}

	public boolean stop(String player) {

		// Existing player found!
		if (instances.containsKey(player)) {
			instances.remove(player).stop();
			return true;
		}
		return false;
	}

	public void stopAll() {

		for (JingleNotePlayer notePlayer : instances.values()) {
			notePlayer.stop();
		}

		instances.clear();
	}

	public void playNear(Location near,double area, MidiJingleSequencer seq) {
		JingleNotePlayer pl = new JingleNotePlayer(seq,area);
		playingNearby.put(pl, near);

	}
}