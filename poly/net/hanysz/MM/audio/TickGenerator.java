package net.hanysz.MM.audio;

/* TickGenerator--part of the Magic Metronome project
 *
 * given a SoundCollection, generate buffers of sound.
 *
 * The "pending" list keeps track of sounds which don't fit into the current buffer.
 * In this implementation, it is a list of pairs of integers,
 * consisting of sound number and where (byte position) to continue each sound.
 *
 * public methods:
 *   public TickGenerator(SoundCollection sounds, boolean greedyMemory)
 *   public TickGenerator(SoundCollection sounds) -- defaults to greedyMemory
 *   public synchronized void setSoundCollection(SoundCollection sounds)
 *   public synchronized void setSound(int whichSound)
 *   public synchronized void setTempo(float tempo)
 *   public float getTempo()
 *   public void setGain(float gain)
 *   public synchronized void resetPosition()
 *   public synchronized void makeNextBuffer(byte[] buffer)
 *
 * to do:
 *   find a better way to implement the "pending" queue
 *   (using LinkedList is very inefficient! -- but ArrayList doesn't implement Queue)
 *
 */


import java.util.*;
import javax.sound.sampled.*;


public class TickGenerator implements net.hanysz.MM.MMConstants {
    private SoundCollection sounds, scaledSounds;
    private int currentSound=0;
    private int lengthOfSoundInFrames;
//    private static float defaultTempo=60.0f;
    private float tempo;
    private int framesPerTick;
    private int tickNumber=0, framesAfterTick=0;
    Queue<Integer> pending=new LinkedList<Integer>();
    float gain=1.0f;
    private boolean greedyMemory;
    private boolean gainHasChanged=true, soundHasChanged=true;
    private final static boolean GREEDY_MEMORY=true,
    				 NO_GREEDY_MEMORY=false;
/* "Greedy memory" mode stores a new copy of all audio data
 * for each new TickGenerator object.  The copy is scaled
 * to the current volume, so that there is no need to do
 * lots of floating point calculations for each new buffer.
 */


    public TickGenerator(SoundCollection sounds, boolean greedyMemory) {
	setSoundCollection(sounds);
	this.greedyMemory = greedyMemory;
	if (greedyMemory) {
	    scaledSounds = new SoundCollection(sounds.getSampleRate(),
	    					sounds.getSampleSizeInBits());
	    scaledSounds.setAudioFormat(sounds.getAudioFormat());
	}
    }


    public TickGenerator(SoundCollection sounds) {
	this(sounds, GREEDY_MEMORY);
    }


    public synchronized void setSoundCollection(SoundCollection sounds) {
	this.sounds=sounds;
	setTempo(DEFAULT_TEMPO);
	setSound(0);
    }


    public synchronized void setSound(int whichSound) {
    	if (whichSound<0) {
	    error("setSound: sound number must be non-negative.");
	}
	if (whichSound>=sounds.getNumberOfSounds()) {
	    error("setSound: sound number "+whichSound+" does not exist.");
	}
	currentSound = whichSound;
	lengthOfSoundInFrames=sounds.getLengthInFrames(whichSound);
	soundHasChanged = true;
    }


    public synchronized void setTempo(float tempo) {
	int framesRemainingInCurrentTick = framesPerTick - framesAfterTick;
	if (tempo<=0) {
	    error("tempo must be a positive number.");
	}
	this.tempo=tempo;
	framesPerTick=(int)(60*sounds.getSampleRate()/tempo);
	    // this is a little bit naughty: we're only using PCM data,
	    // so sample rate = frame rate always

        if (framesPerTick==0) {framesPerTick=2;}
                        // framesPerTick=0 will send makeNextBuffer into an infinite loop!

	// adjust framesAfterTick in case the tempo changed in the middle of a tick:
	framesAfterTick = framesPerTick - framesRemainingInCurrentTick;
	if (framesAfterTick < 0) {
	    framesAfterTick = 0;
	    tickNumber++;
	}
    }


    public float getTempo() {
	return tempo;
    }
    

    public void setGain(float gain) {
	if (gain<0) {
	    error("trying to set negative gain.");
	}
	this.gain=gain;
	gainHasChanged = true;
    }


    public synchronized void resetPosition() {
	framesAfterTick = 0;
    }


    public void setGainOfStoredData(int whichSound) {
	// first make sure that scaledSounds has a place for the data!
	for (int i = scaledSounds.getNumberOfSounds(); i<= whichSound; i++) {
	    scaledSounds.addVoidSound();
	}
	// now copy and scale:
	scaledSounds.copySoundFromCollection(whichSound, sounds, whichSound);
		// whichSound argument appears twice: the sound occupies
		// the same slot in each SoundCollection
	scaledSounds.changeVolume(whichSound, gain);
	gainHasChanged = false;
	soundHasChanged = false;
    }


    private synchronized void addTickToBuffer(int theSound, int soundPos,
    				 SoundBuffer buffer, int bufferPos, int bufferSize) {
	int framesToAdd=sounds.getLengthInFrames(theSound)-soundPos;
	int roomInBuffer=bufferSize-bufferPos;

	if (framesToAdd <= roomInBuffer) {
	    if (greedyMemory) {
	    	scaledSounds.addSoundToBuffer(
	    		theSound,soundPos,buffer,bufferPos,framesToAdd);
	    } else {
		sounds.addSoundToBuffer(
			theSound,soundPos,buffer,bufferPos,framesToAdd,gain);
	    }
	} else {
	    if (greedyMemory) {
	    	scaledSounds.addSoundToBuffer(
			theSound,soundPos,buffer,bufferPos,roomInBuffer);
	    } else {
		sounds.addSoundToBuffer(
			theSound,soundPos,buffer,bufferPos,roomInBuffer,gain);
	    }
	    // pending.add(theSound);
	    // pending.add(soundPos+roomInBuffer);
	    addToPendingQueue(theSound, soundPos+roomInBuffer);
	}
    }


    private void addToPendingQueue(int theSound, int soundResumePos) {
	pending.add(theSound);
	pending.add(soundResumePos);
    }


    private synchronized void doPending(SoundBuffer buffer, int bufferSize) {
	int theSound, soundPos, numberPending;
	numberPending=pending.size()/2;
	for (int i=0; i<numberPending; i++) {
	    theSound=pending.remove();
	    soundPos=pending.remove();
	    addTickToBuffer(theSound, soundPos, buffer, START_OF_BUFFER, bufferSize);
	}
    }


    public synchronized void makeNextBuffer(SoundBuffer buffer, int numberOfFrames) {
    // usually, numberOfFrames will equal the size of the buffer,
    // but it might sometimes be useful to fill just part of a buffer?
	int bufferPos=0, bufferSize = numberOfFrames;

	doPending(buffer, bufferSize);

	if (greedyMemory && (gainHasChanged || soundHasChanged)) {
	    setGainOfStoredData(currentSound);
	}
	while (bufferPos<numberOfFrames) {
	    if (framesAfterTick>0) {
		bufferPos+=(framesPerTick-framesAfterTick);
		framesAfterTick=0;
		tickNumber++;
	    }
	    if (bufferPos<numberOfFrames) {
		addTickToBuffer(currentSound, START_OF_BUFFER, buffer, bufferPos, bufferSize);
		tickNumber++;
		bufferPos+=framesPerTick;
	    }
	} //end while
	if (bufferPos>bufferSize) {
	    tickNumber--;
	    framesAfterTick=framesPerTick-bufferPos+bufferSize;
	}
    } // end makeNextBuffer


/* this has been replaced by SoundBuffer.clearBuffer():

    public synchronized void clearBuffer(byte[] buffer) {
	for (int i=0; i<bufferSize; i++) {
	    buffer[i]=0;
	}
    }
*/


    private void error (String message) {
        System.out.println("Error in class TickGenerator: "
                            + message);
        System.exit(1);
    }
}
