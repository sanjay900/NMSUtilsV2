package net.tangentmc.nmsUtils.jinglenote;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import net.tangentmc.nmsUtils.jinglenote.JingleSequencer.Note;

public class JingleNotePlayer implements Runnable {

	protected JingleSequencer sequencer;
	Set<Player> playingTo = Sets.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<Player,Boolean>()));
	public double area;
	/**
	 * Constructs a new JingleNotePlayer
	 * 
	 * @param player The player who is hearing this's name.
	 * @param seq The JingleSequencer to play.
	 * @param area The SearchArea for this player. (optional)
	 */
	public JingleNotePlayer(JingleSequencer seq,double area, String...player) {
		this.area = area;
		playingTo.addAll(Arrays.stream(player).map(Bukkit::getPlayerExact).collect(Collectors.toSet()));
		sequencer = seq;
	}

	@Override
	public void run() {

		if(sequencer == null)
			return;
		try {
			try {
				sequencer.play(this);
			} catch (Throwable t) {
				t.printStackTrace();
			}

			while(isPlaying()){
				Thread.sleep(10L);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			stop();
		}
	}

	public boolean isPlaying() {
		return (sequencer != null && (sequencer.isPlaying() || !sequencer.hasPlayedBefore()));
	}

	public void stop() {

		if (sequencer != null) {
			sequencer.stop(this);
			sequencer = null;
		}
	}

	public void play (Note note)  {

		if(!isPlaying()) return;
		playingTo.forEach(p->{
			p.playSound(p.getLocation(), toSound(note.getInstrument()), note.getVelocity(), note.getNote());
		});
	}
	public Sound toSound(Instrument instrument) {

		switch(instrument) {
		case PIANO:
			return Sound.BLOCK_NOTE_HARP;
		case GUITAR:
			return Sound.BLOCK_NOTE_PLING;
		case BASS:
			return Sound.BLOCK_NOTE_BASS;
		case BASS_GUITAR:
			return Sound.BLOCK_NOTE_BASS;
		case STICKS:
			return Sound.BLOCK_NOTE_SNARE;
		case BASS_DRUM:
			return Sound.BLOCK_NOTE_BASEDRUM;
		case SNARE_DRUM:
			return Sound.BLOCK_NOTE_SNARE;
		default:
			return Sound.BLOCK_NOTE_HARP;
		}
	}

	public void removePlayer(Player pl) {
		this.playingTo.remove(pl);
	}

	public void playTo(Player pl) {
		this.playingTo.add(pl);
	}
}