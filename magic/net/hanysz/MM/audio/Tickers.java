package net.hanysz.MM.audio;

/* Tickers -- part of the Magic Metronome project
 *
 * given a SoundCollection, use TickGenerator to make a repeated tick.
 * Runs as a separate Thread.
 *
 * public methods:
 *   public Tickers(SoundCollection sounds,  int bufferSize)
 *   public void setSoundCollection(SoundCollection sounds)
 *   public void setSound(int whichSound, int track)
 *   public void setTempo(float tempo, int track)
 *   public void getTempo(int track)
 *   public void setGain(float gain, int track)
 *   public void stopPlaying(int track);
 *   public void startPlaying(int track);
 *   public boolean isPlaying(int track)
 *   public void run();
 *   public void exit();
 *
 * Latest update: 28th December: multi-track version
 */


import java.util.*;
import javax.sound.sampled.*;


public class Tickers extends Thread {
    private SoundCollection sounds;
    private boolean playing=false;
    private int numberOfTracks, numberOfTracksPlaying=0;
    private SoundBuffer buffer;
    private int bufferSize;
    private List<TickGenerator> tickGenerators = new ArrayList<TickGenerator>();
    private List<Boolean> isPlaying = new ArrayList<Boolean>();
    private SourceDataLine line;


    public Tickers(SoundCollection sounds, int bufferSize) {
	this.sounds=sounds;
	this.bufferSize=bufferSize;
	this.numberOfTracks=numberOfTracks;
	buffer=new SoundBuffer(sounds.getAudioFormat(), bufferSize);
	/*
	isPlaying = new boolean[numberOfTracks];
	tickGenerators = new TickGenerator[numberOfTracks];
	for (int i=0; i<numberOfTracks; i++) {
	    tickGenerators[i] = new TickGenerator(sounds);
	    isPlaying[i]=false;
	}
	*/
	line = openAudioLine(sounds.getAudioFormat());
    }


    public void addTrack() {
	tickGenerators.add(new TickGenerator(sounds));
	isPlaying.add(false);
	numberOfTracks++; // do this last for thread safety
    }


    public void removeTrack() {
	numberOfTracks--;
	tickGenerators.remove(numberOfTracks); // garbage collection should destroy the object!
	isPlaying.remove(numberOfTracks);
    }


    public static SourceDataLine openAudioLine(AudioFormat audioFormat) {
        SourceDataLine line=null;
        DataLine.Info   info = new DataLine.Info(SourceDataLine.class,
                                         audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return line;
    }



    public void setSoundCollection(SoundCollection sounds, int track) {
	tickGenerators.get(track).setSoundCollection(sounds);
    }


    public void setSound(int whichSound, int track) {
	tickGenerators.get(track).setSound(whichSound);
    }


    public void setTempo(float tempo, int track) {
	tickGenerators.get(track).setTempo(tempo);
    }


    public float getTempo(int track) {
	return tickGenerators.get(track).getTempo();
    }


    public void setGain(float gain, int track) {
	tickGenerators.get(track).setGain(gain);
    }


    public void startPlaying(int track) {
	if (!isPlaying.get(track)) {
	    isPlaying.set(track,true);
	    numberOfTracksPlaying++;
	}
	tickGenerators.get(track).resetPosition();
	// line.flush(); -- deleted because it causes other track to "stutter".
    }


    public void stopPlaying(int track) {
	if (isPlaying.get(track)) {
	    isPlaying.set(track, false);
	    numberOfTracksPlaying--;
	}
    }


    public boolean isPlaying(int track) {
	return isPlaying.get(track);
    }


    public void run () {
        playing=true;
        line.start();
            while (playing) {
		while (numberOfTracksPlaying==0) {
		    try {Thread.sleep(100);}
		    catch (InterruptedException e) {/*do nothing*/}
		}
		buffer.clearBuffer();
		for (int i=0; i<numberOfTracks; i++) {
		    if (isPlaying.get(i)) {
			tickGenerators.get(i).makeNextBuffer(buffer, bufferSize);
		    }
		}
		buffer.playBuffer(line);
            }
        line.flush();
        line.close();
    }


    public void exit() {
        playing=false;
    }
}
