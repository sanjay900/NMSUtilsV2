package net.tangentmc.nmsUtils.jinglenote;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;


public class MelodyPlayer implements Runnable {

    private JingleNoteManager jNote;
    private MidiJingleSequencer sequencer;
    private boolean isPlaying;

    private final Set<String> toStop, toPlay;
    public MelodyPlayer(File file, boolean loop) throws MidiUnavailableException, InvalidMidiDataException, IOException {
    	this(new MidiJingleSequencer(file, loop));
    }
    public MelodyPlayer(MidiJingleSequencer sequencer) {
        this.sequencer = sequencer;
        jNote = new JingleNoteManager();
        toStop = new HashSet<String>();
        toPlay = new HashSet<String>();
        isPlaying = false;
    }

    public boolean isPlaying(String player) {
        return isPlaying() && (toPlay.contains(player) || jNote.isPlaying(player));
    }

    public void stop(String player) {

        toStop.add(player);
        toPlay.remove(player);
       
    }

    public void play(String player) {

        if(jNote.isPlaying(player) || toPlay.contains(player)) return;
        toPlay.add(player);
        toStop.remove(player);
      
    }

    public boolean isPlaying() {
        return isPlaying && /*(!toPlay.isEmpty() || jNote.isPlaying() ||*/ sequencer != null && (sequencer.isPlaying() || !sequencer.hasPlayedBefore())/*)*/;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public void run () {
        try {
            isPlaying = true;
            

            while(isPlaying) {
                for(String player : toStop)
                    jNote.stop(player);
                toStop.clear();
                for(String player : toPlay) {
                    jNote.play(player, sequencer);
                }
                toPlay.clear();

                if(!isValid() || !isPlaying() && sequencer.hasPlayedBefore()) {
                    isPlaying = false;
                    break;
                }
            }

        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            if(sequencer != null)
                sequencer.stop();
            jNote.stopAll();
            sequencer = null;
            toPlay.clear();
            toStop.clear();
            isPlaying = false;
        }
    }

    public boolean isValid() {
        if(sequencer == null) return false;
        return !(!sequencer.isPlaying() && sequencer.hasPlayedBefore());
    }
}

