package net.hanysz.MM.audio;

/* SoundCollection -- part of the Magic Metronome project
 *
 * load sound data from files;
 * store all sound data plus length of each sound and a name;
 * provide a method to put sound data into a buffer.
 * nb at this stage, all data must be mono.
 *
 * public methods:
 *    public SoundCollection(float sampleRate, int sampleSizeInBits)
 *    public int openFile(File soundFile, String soundName)
 *    public void addSoundToBuffer(int sound, int soundPos,
 *            byte[] buffer, int bufferPos, int numberOfFrames, float gain)
 *    public void addSoundToBuffer(int sound, int soundPos,
                         SoundBuffer buffer, int bufferPos, int numberOfFrames)
 *    public void addVoidSound()
 *    public void copySoundFromCollection(int whichTargetSound, 
 		SoundCollection sounds, int whichSourceSound)
 *    public int getNumberOfSounds()
 *    public float getSampleRate()
 *    public int getSampleSizeInBits()
 *    public int getSampleSizeInBytes()
 *    public int getFrameSizeInBytes()
 *    public AudioFormat getAudioFormat()
 *    public void setAudioFormat(AudioFormat audioFormat)
 *    public String getName(int sound)
 *    public int getLengthInBytes(int sound)
 *    public int getLengthInFrames(int sound)
 *
 * to do: implement some sort of exception handling;
 *        modify openFile to check that audioFormats are all compatible;
 *		nb check endianness!
 *        write "openFilesInDirectory" method;
 *        error checking for getName and getLength... methods.
 *
 * latest update: 1st January 2007
 */


import java.util.*;
import java.io.File;
import java.net.URL;
import java.io.IOException;
import javax.sound.sampled.*;


public class SoundCollection implements net.hanysz.MM.MMConstants {
    // private int maxNumberOfSounds;
    private int numberOfSounds=0;
    private float sampleRate;
    private int sampleSizeInBits;
    private int sampleSizeInBytes;
    private int frameSizeInBytes;
    private AudioFormat audioFormat = null;
    private boolean formatIsBigEndian;
    private List<SoundBuffer> audioData = new ArrayList<SoundBuffer>();
    private List<Integer> lengthInBytes = new ArrayList<Integer>();
    private List<Integer> lengthInFrames = new ArrayList<Integer>();
    private List<String> names = new ArrayList<String>();
    private boolean debugging=false;
    private SourceDataLine audioLine;
    private int numberOfChannels = 1;  // must rework everything in stereo some time!
    static private final int MONO_CHANNEL = 0;


    public SoundCollection(float sampleRate, int sampleSizeInBits) {
	if ((sampleSizeInBits % 8)!=0) {
	    error("Sample size must be a multiple of 8 bits.");
	}
	if (sampleSizeInBits>32) {
	    error("Sample size of "+sampleSizeInBits+" bits is too large "+
		    "(maximum is 32).");
	}
	this.sampleRate = sampleRate;
	this.sampleSizeInBits = sampleSizeInBits;
	sampleSizeInBytes = sampleSizeInBits/8; // redundant, but convenient
	frameSizeInBytes = sampleSizeInBytes * numberOfChannels;
    }


    public void erase(int whichSound) {
	audioData.get(whichSound).clearBuffer();
    }


    public int openURL(URL soundURL, String soundName) {
	AudioInputStream audioInputStream = null; // must be initialised!
	AudioFileFormat audioFileFormat = null;
	AudioFormat audioFormat;
	int fileLengthInBytes;
	byte[] tempBuffer;

	/*  not necessary now we've changed implementation to List:
	if (numberOfSounds==maxNumberOfSounds) {
	    error("openFile: reached max number of sounds!");
	}
	*/

	/* First check length and format of file
	 *   To do: handle errors more gracefully;
	 *          if file is wrong format, it may
	 *          be possible to convert it.
	 */

	try {
	    audioFileFormat = AudioSystem.getAudioFileFormat(soundURL);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	fileLengthInBytes = audioFileFormat.getFrameLength()*frameSizeInBytes;
	// warning: this assumes frames=samples, which is true for PCM data but not mp3 etc

	try {
	    audioInputStream = AudioSystem.getAudioInputStream(soundURL);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	audioFormat = audioInputStream.getFormat();
	if ( Math.abs(audioFormat.getSampleRate()-sampleRate)>1) {
	    error("file has wrong sample rate.");
	    System.exit(1);
	}
	if (audioFormat.getSampleSizeInBits()!=sampleSizeInBits) {
	    error("file has wrong sample size.");
	}
	if (audioFormat.getChannels()!=1) {
	    error("file is not mono.");
	}
	// should check endianness too...
	this.audioFormat = audioFormat; //should be the same every time!
	formatIsBigEndian=audioFormat.isBigEndian();

	/*Now read the data.
	 * (this is the lazy way, assuming we can grab it all in one chunk.
	 *  Should fix this some time.)
	 */
	int nBytesRead = 0;
	int bytesReadThisTime = 0;
	tempBuffer = new byte[fileLengthInBytes];
	try {
	    while (bytesReadThisTime != -1) {
		bytesReadThisTime = audioInputStream.read(tempBuffer,
			nBytesRead, fileLengthInBytes);
		nBytesRead+=bytesReadThisTime;
		if (debugging) {
		    System.out.println("     got chunk of " + bytesReadThisTime + " bytes.");
		}
	    } // end while
	} catch (IOException e) {
	    e.printStackTrace();
	}
	nBytesRead++; // we added a -1 the last time round the "while" loop...
	if (nBytesRead == 0) {
	    error("no data in sound file.");
	}
	/* temporarily disabled until I find a better way:
	if (nBytesRead != fileLengthInBytes) {
	    error("unable to read all data from sound file.  Read "+
	    	   nBytesRead+" bytes out of "+fileLengthInBytes+".");
	}
	*/

	int fileLengthInFrames = fileLengthInBytes / frameSizeInBytes;
	SoundBuffer newBuffer = new SoundBuffer(audioFormat, fileLengthInFrames);
	newBuffer.copyBytesToBuffer(
		START_OF_BUFFER, tempBuffer,
		START_OF_BUFFER, fileLengthInFrames);
	audioData.add(newBuffer);

	// tidy up and return:
	lengthInBytes.add(nBytesRead);
	lengthInFrames.add(nBytesRead/frameSizeInBytes);
	names.add(new String(soundName));
	numberOfSounds++;
	if (debugging)
	    System.out.println("openURL read "+nBytesRead
		+" bytes from file of length "+fileLengthInBytes+" bytes.");
	return (numberOfSounds-1);
    } // end openURL


    public int openFile(File soundFile, String soundName) {
	URL soundURL = null;
	try {
	    soundURL = new URL("file:"+soundFile.getAbsolutePath());
	} catch (java.net.MalformedURLException e) {error("openFile: can't make URL");}
	return openURL(soundURL, soundName);
    }


/* -- no longer used -- replaced by method in SoundBuffer class
    private int bytesToSample(byte[] buffer, int bufferPos) {
	int sampleValue=0;
	if (formatIsBigEndian) {
	    sampleValue=buffer[bufferPos];  // do this first so it has the correct sign
	    for (int i=bufferPos+1; i<bufferPos+sampleSizeInBytes; i++) {
		sampleValue=(sampleValue<<8) | (buffer[i]&255);  // the less significant bytes are unsigned
	    }
	} else {
	    sampleValue=buffer[bufferPos+sampleSizeInBytes-1];
	    for (int i=bufferPos+sampleSizeInBytes-2; i>=bufferPos; i--) {
		sampleValue=(sampleValue<<8) | (buffer[i]&255);
	    }
	}
	return sampleValue;
    }


    private void sampleToBytes(byte[] buffer, int bufferPos, int sampleValue) {
	if (formatIsBigEndian) {
	    for (int i=bufferPos+sampleSizeInBytes-1; i>bufferPos; i--) {
		buffer[i]=(byte)(sampleValue & 255);
		sampleValue >>= 8; // nb >> keeps sign
	    }
	    buffer[bufferPos]=(byte)sampleValue;
	} else {
	    for (int i=bufferPos; i<bufferPos+sampleSizeInBytes-1; i++) {
		buffer[i]=(byte)(sampleValue & 255);
		sampleValue >>= 8;
	    }
	    buffer[bufferPos+sampleSizeInBytes-1]=(byte)sampleValue;
	}
    }
*/


    public void addSoundToBuffer(int sound, int soundPos,
		     SoundBuffer buffer, int bufferPos, int numberOfFrames, float gain) {
    // nb this method _adds_ the sound to whatever values are already stored in the buffer,
    // rather than replacing the contents.

	int oldSampleValue, newSampleValue;
	// First check that parameters are valid:
	if (sound>=numberOfSounds) {
	    error("addSoundToBuffer: invalid sound number "+sound);
	}
	if (soundPos<0) {
	    error("addSoundToBuffer: negative soundPos ("+soundPos+").");
	}
	if (bufferPos<0) {
	    error("addSoundToBuffer: negative bufferPos ("+bufferPos+").");
	}
	if (bufferPos+numberOfFrames>buffer.getLengthInFrames()) {
	    error("addSoundToBuffer: too many frames--buffer will overflow.");
	}
	if (soundPos+numberOfFrames>lengthInFrames.get(sound)) {
	    error("addSoundToBuffer: too many frames--trying to read past end of sound.");
	}
	// now copy the data:
	for (int i=0; i<numberOfFrames; i++) {
	    oldSampleValue=buffer.getSample(bufferPos+i, MONO_CHANNEL); // channel 0 for mono
	    newSampleValue=audioData.get(sound).getSample(soundPos+i, MONO_CHANNEL);
	    newSampleValue=(int)(newSampleValue*gain);
	    buffer.setSample(bufferPos+i, MONO_CHANNEL, buffer.addSamples(oldSampleValue, newSampleValue));
	}
    } //end addSoundToBuffer, with gain argument


    public void addSoundToBuffer(int sound, int soundPos,
                         SoundBuffer buffer, int bufferPos, int numberOfFrames) {
	// similar to previous method, but without the floating point calculations...
	int oldSampleValue, newSampleValue;

        if (sound>=numberOfSounds) {
            error("addSoundToBuffer: invalid sound number "+sound);
        }
        if (soundPos<0) {
            error("addSoundToBuffer: negative soundPos ("+soundPos+").");
        }
        if (bufferPos<0) {
            error("addSoundToBuffer: negative bufferPos ("+bufferPos+").");
        }
        if (bufferPos+numberOfFrames>buffer.getLengthInFrames()) {
            error("addSoundToBuffer: too many frames--buffer will overflow.");
        }
        if (soundPos+numberOfFrames>lengthInFrames.get(sound)) {
            error("addSoundToBuffer: too many frames--trying to read past end of sound.");
        }

	for (int i=0; i<numberOfFrames; i++) {
	    oldSampleValue=buffer.getSample(bufferPos+i, MONO_CHANNEL); // channel 0 for mono
	    newSampleValue=audioData.get(sound).getSample(soundPos+i, MONO_CHANNEL);
	    buffer.setSample(bufferPos+i, MONO_CHANNEL, buffer.addSamples(oldSampleValue, newSampleValue));
	}
    } //end addSoundToBuffer, without gain argument


    public void addVoidSound() {
	audioData.add(null);
	numberOfSounds++;
	lengthInFrames.add(0);
	lengthInBytes.add(0);
	names.add("");
    }


    public void copySoundFromCollection(int whichTargetSound, 
 		SoundCollection sounds, int whichSourceSound) {
	// this will delete the old contents of whichTargetSound

	int framesToCopy = sounds.lengthInFrames.get(whichSourceSound);
	SoundBuffer copiedAudioData = new SoundBuffer(audioFormat, framesToCopy);
    	copiedAudioData.copyToBuffer(START_OF_BUFFER,
				     sounds.audioData.get(whichSourceSound),
				     START_OF_BUFFER, framesToCopy);
	audioData.set(whichTargetSound, copiedAudioData);
	lengthInFrames.set(whichTargetSound, sounds.lengthInFrames.get(whichSourceSound));
	lengthInBytes.set(whichTargetSound, sounds.lengthInBytes.get(whichSourceSound));
	names.set(whichTargetSound, sounds.names.get(whichSourceSound));
    }


    public void changeVolume(int whichSound, float gain) {
	audioData.get(whichSound).changeVolume(gain);
    }


    public int getNumberOfSounds() {
	return numberOfSounds;
    }


    public float getSampleRate() {
	return sampleRate;
    }


    public int getSampleSizeInBits() {
	return sampleSizeInBits;
    }


    public int getSampleSizeInBytes() {
	return sampleSizeInBytes;
    }


    public int getFrameSizeInBytes() {
	return frameSizeInBytes;
    }


    public AudioFormat getAudioFormat() {
	return audioFormat;
    }


    public void setAudioFormat(AudioFormat audioFormat) {
    // warning: if the audioFormat is already set, changing it will cause problems!
	this.audioFormat = audioFormat;
    }


    public String getName(int sound) {
	return names.get(sound);
    }


    public int getLengthInBytes(int sound) {
	return lengthInBytes.get(sound);
    }


    public int getLengthInFrames(int sound) {
	return lengthInFrames.get(sound);
    }


    private void error (String message) {
	System.out.println("Error in class SoundCollection: "
			    + message);
	System.exit(1);
    }

}
