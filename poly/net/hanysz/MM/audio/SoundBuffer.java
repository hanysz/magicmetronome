package net.hanysz.MM.audio;

/* SoundBuffer -- part of the Magic Metronome project
 *
 * encapsulate audio data: store a buffer of samples
 *
 * (samples are stored as ints: this may look inefficient (32 bits to
 * store a 16 or 24 bit sample), but it's important to have room for
 * overflow when adding samples or changing the volume.)
 *
 * in fact, the class stores two buffers: the samples need to be converted
 * to bytes before the sound can be played.
 *
 * public methods:
 *    public SoundBuffer(AudioFormat audioFormat, int bufferSizeInFrames)
 *    public void clearBuffer()
 *    public void playBuffer(SourceDataLine line)
 *    public void copyToBuffer(int offset, SoundBuffer otherBuffer,
 *			         int startPos, int numberOfFrames)
 *    public void addToBuffer(int offset, SoundBuffer otherBuffer,
 *			         int startPos, int numberOfFrames)
 *    public void changeVolume(float gain)
 *    public int getSample(int whichFrame, int whichChannel)
 *    public void setSample(int whichFrame,
 *		           int whichChannel, int sampleValue)
 *    public int addSamples(int firstSample, int secondSample)
 *    public int addSamples(int[] samples)
 *    public void copyBufferToBytes(int offset, byte[] bytes,
 *			           int startPos, int numberOfFrames)
 *    public void copyBytesToBuffer(int offset, byte[] bytes,
 *			           int startPos, int numberOfFrames)
 *    public int bytesToSample(byte[] buffer, int bufferPos)
 *    public void sampleToBytes(byte[] buffer, int bufferPos, int sampleValue)
 *    public AudioFormat getAudioFormat()
 *    public int getLengthInFrames();
 *
 */


import javax.sound.sampled.*;


public class SoundBuffer implements net.hanysz.MM.MMConstants {
//    public static final int START_OF_BUFFER = 0;
//    public static final int START_OF_BYTES = 0;
// constant declarations moved to file MMConstants.java
    private int[] buffer;
    private byte[] bufferAsBytes;
    private int bufferSizeInFrames, bufferSizeInSamples, bufferSizeInBytes;
    private AudioFormat audioFormat;
    private int sampleSizeInBits, sampleSizeInBytes;
    private int maxSampleValue, minSampleValue;
    private int numberOfChannels;
    private float sampleRate;
    private boolean formatIsBigEndian;
    // SourceDataLine audioLine; -- not used?
    private boolean debugging = false;


    public SoundBuffer(AudioFormat audioFormat, int bufferSizeInFrames) {
	this.bufferSizeInFrames = bufferSizeInFrames;
	this.audioFormat = audioFormat;
	sampleSizeInBits = audioFormat.getSampleSizeInBits();
        if ((sampleSizeInBits % 8)!=0) {
            error("Sample size must be a multiple of 8 bits.");
        }
        if (sampleSizeInBits > 32) {
            error("Sample size of "+sampleSizeInBits+" bits is too large "+
                    "(maximum is 32).");
        }
	sampleSizeInBytes = sampleSizeInBits/8;
	maxSampleValue = (int)(Math.pow(2,sampleSizeInBits-1)-1);
	minSampleValue = -maxSampleValue;
	numberOfChannels = audioFormat.getChannels();
	sampleRate = audioFormat.getSampleRate();
	formatIsBigEndian = audioFormat.isBigEndian();

	bufferSizeInSamples = bufferSizeInFrames * numberOfChannels;
	bufferSizeInBytes = bufferSizeInSamples * sampleSizeInBytes;
	buffer = new int[bufferSizeInSamples];
	bufferAsBytes = new byte[bufferSizeInBytes];
    }


    public void clearBuffer() {
	for (int i = 0; i < bufferSizeInSamples; i++) {
	    buffer[i] = 0;
	}
    }


    public void playBuffer(SourceDataLine line) {
	copyBufferToBytes(START_OF_BYTES, bufferAsBytes, START_OF_BUFFER, bufferSizeInFrames);
	line.write(bufferAsBytes, START_OF_BYTES, bufferSizeInBytes);
    }


    public void copyToBuffer(int offset, SoundBuffer otherBuffer,
				  int startPos, int numberOfFrames) {
	int numberOfSamples = numberOfFrames * numberOfChannels;
	for (int i = 0; i < numberOfSamples; i++) {
	    buffer[offset++] = otherBuffer.buffer[startPos++];
	}
    }


    public void addToBuffer(int offset, SoundBuffer otherBuffer,
				  int startPos, int numberOfFrames) {
	int numberOfSamples = numberOfFrames * numberOfChannels;
	int oldSample;
	for (int i = 0; i < numberOfSamples; i++) {
	    oldSample = buffer[offset];
	    buffer[offset++] = addSamples(oldSample, otherBuffer.buffer[startPos++]);
	}
    }


    public void changeVolume(float gain) {
	int sampleValue;
	for (int channel = 0; channel < numberOfChannels; channel++) {
	    for (int i = 0; i < bufferSizeInFrames; i++) {
		sampleValue = (int)(getSample(i, channel) * gain);
		sampleValue = clipSample(sampleValue);
		setSample(i, channel, sampleValue);
	    }
	}
    }


    public int getSample(int whichFrame, int whichChannel) {
	return buffer[whichFrame*numberOfChannels + whichChannel];
    }


    public void setSample(int whichFrame,
			    int whichChannel, int sampleValue) {
	buffer[whichFrame*numberOfChannels + whichChannel] =
	    sampleValue;
    }


    public int clipSample(int sample) {
	// check for overflow, and clip if necessary:
	if (sample > maxSampleValue) {
	    sample = maxSampleValue;
	}
	if (sample < minSampleValue) {
	    sample = minSampleValue;
	}
	return sample;
    }


    public int addSamples(int firstSample, int secondSample) {
	int newSample = firstSample + secondSample;
	return clipSample(newSample);
    }


    public int addSamples(int[] samples) {
	int newSample=0;
	for (int i=0; i<samples.length; i++) {
	    newSample+=samples[i];
	}
	return clipSample(newSample);
    }


    public void copyBufferToBytes(int offset, byte[] bytes,
				    int startPos, int numberOfFrames) {
	int numberOfSamples = numberOfFrames * numberOfChannels;
	int sampleValue;
	for (int i=0; i < numberOfSamples; i++) {
	    sampleValue = buffer[offset++];
	    sampleToBytes(bytes, startPos, sampleValue);
	    startPos += sampleSizeInBytes;
	}
    }


    public void copyBytesToBuffer(int offset, byte[] bytes,
				    int startPos, int numberOfFrames) {
	int numberOfSamples = numberOfFrames * numberOfChannels;
	if (debugging) {
	    System.out.println("Calling copyBytesToBuffer: numberOfSamples="+numberOfSamples);
	    System.out.println("Number of frames="+numberOfFrames);
	}
	for (int i=0; i < numberOfSamples; i++) {
	    buffer[offset++] = bytesToSample(bytes, startPos);
	    startPos += sampleSizeInBytes;
	}
    }


    public int bytesToSample(byte[] buffer, int bufferPos) {
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


    public void sampleToBytes(byte[] buffer, int bufferPos, int sampleValue) {
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


    public AudioFormat getAudioFormat() {
	return audioFormat;
    }


    public int getLengthInFrames() {
	return bufferSizeInFrames;
    }


    private void error (String message) {
        System.out.println("Error in class SoundBuffer: "
                            + message);
        System.exit(1);
    }
}

