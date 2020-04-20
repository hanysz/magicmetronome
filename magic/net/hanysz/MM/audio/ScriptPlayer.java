package net.hanysz.MM.audio;

/* ScriptPlayer--part of the Magic Metronome project
 *
 * given a SoundCollection, and an MMEventList, generate buffers of sound.
 *
 * modelled on TickGenerator (but a bit more complex)
 *
 *  latest update: 27th January 2007
 */


import java.util.*;
import javax.sound.sampled.*;
import net.hanysz.MM.events.*;
import net.hanysz.MM.MMgui;


public class ScriptPlayer extends Thread implements net.hanysz.MM.MMConstants {
    private MMEventList eventList;
    private SoundCollection sounds;
    // private int currentSound=0; -- no longer used?
    // private int lengthOfSoundInFrames; -- no longer used?
    private float tempo;
    private int accelMode = NO_ACCEL_MODE;
    private float accelFactor = 1.0f;
    private float accelStartTempo, accelEndTempo;
    private int framesPerTick;
    private int tickNumber=0, framesAfterTick=0;
    private long framePosition = 0;
    private long initialFramePosition = 0;
    private Queue<Integer> pending=new LinkedList<Integer>();
    private Queue<Float> pendingVolumes=new LinkedList<Float>();
    private Stack<Float> tempoStack = new Stack<Float>();
    private List<ScriptPlayer> branchList = new ArrayList<ScriptPlayer>();
    // private List<Long> branchFramePositions = new ArrayList<Long>();
    // store start time of each branch, so that it's possible to start
    // playing in the middle of a script
    // replaced by "initial frame position" : the position is stored by the child not the parent

    // some useful flags:
    private boolean lookingForMarker = false;
    private boolean makingBranches = false;
    private boolean seeking = false;
    private String markerToFind;
    private boolean rewinding = false; // sometimes we need to go back to a previous event
    private MMEvent rememberedEvent;

    private boolean playing = true;
    private boolean paused = false;
    // nb after end of track, there might be more sounds on pending list
    // so we can't finish until pending is cleared
    private boolean endOfTrack = false;

    private boolean verbose = net.hanysz.MM.MM.verbose;

    float volume=1.0f, currentVolume=1.0f;
    // volume is track volume; currentVolume is relative volume of current sound
    boolean gotVolume = false; // set this flag when a volume event comes in

    int branchDepth = 0; // keep track of how many times we've branched;
    			// not sure why it might be useful, but there's no harm in it!

    private int bufferSize = 0;
    private SoundBuffer buffer;


    public ScriptPlayer(MMEventList eventList, SoundCollection sounds,
		    int bufferSize, long initialFramePosition) {
	this.eventList = eventList;
	this.initialFramePosition = initialFramePosition;
	this.bufferSize = bufferSize;
	setSoundCollection(sounds);
	setTempo(DEFAULT_TEMPO);
    }


    public ScriptPlayer(MMEventList eventList, SoundCollection sounds, long initialFramePosition) {
	this(eventList, sounds, 0, initialFramePosition);
    }
    // bufferSize is only needed for the top-level branch, not for children


    public ScriptPlayer(MMEventList eventList, SoundCollection sounds) {
	this(eventList, sounds, 0, 0);
    }


    public synchronized void setSoundCollection(SoundCollection sounds) {
	this.sounds=sounds;
    }


    /* not used (inherited from TickGenerator) ?
    public synchronized void setSound(int whichSound) {
    	if (whichSound<0) {
	    error("setSound: sound number must be non-negative.");
	}
	if (whichSound>=sounds.getNumberOfSounds()) {
	    error("setSound: sound number "+whichSound+" does not exist.");
	}
	currentSound = whichSound;
	lengthOfSoundInFrames=sounds.getLengthInFrames(whichSound);
    }
    */


    public synchronized void setTempo(float tempo) {
// needs to be modified: framesRemainingInCurrentTick shouldn't be necessary,
// as tempo won't change in the middle of a tick
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
    

    public void setVolume(float volume) {
	if (volume<0) {
	    error("trying to set negative volume.");
	}
	this.volume=volume;
    }


    /* not used? --
    public synchronized void resetPosition() {
	framesAfterTick = 0;
    }
    */


    public boolean isPlaying() {
	return playing;
    }


    private synchronized void addTickToBuffer(int theSound, float soundVolume, int soundPos,
    				 SoundBuffer buffer, int bufferPos, int bufferSize) {
	if (theSound > sounds.getNumberOfSounds()-1) {
	    theSound=SILENT_SOUND;
	}
	if (theSound != SILENT_SOUND) {
	    int framesToAdd=sounds.getLengthInFrames(theSound)-soundPos;
	    int roomInBuffer=bufferSize-bufferPos;
	    if (framesToAdd <= roomInBuffer) {
		sounds.addSoundToBuffer(
			theSound,soundPos,buffer,bufferPos,framesToAdd,volume*soundVolume);
	    } else {
		sounds.addSoundToBuffer(
			theSound,soundPos,buffer,bufferPos,roomInBuffer,volume*soundVolume);
		addToPendingQueue(theSound, soundPos+roomInBuffer);
		addToPendingVolumes(soundVolume);
	    }
	}
    }


    private synchronized void addTickToBuffer(int theSound, int soundPos,
    				 SoundBuffer buffer, int bufferPos, int bufferSize) {
	addTickToBuffer(theSound, 1.0f, soundPos, buffer, bufferPos, bufferSize);
    }


    private void addToPendingQueue(int theSound, int soundResumePos) {
	pending.add(theSound);
	pending.add(soundResumePos);
    }


    private void addToPendingVolumes(float soundVolume) {
	pendingVolumes.add(soundVolume);
    }


    private synchronized void doPending(SoundBuffer buffer, int bufferSize) {
	int theSound, soundPos, numberPending;
	float soundVolume;
	numberPending=pending.size()/2;
	for (int i=0; i<numberPending; i++) {
	    theSound=pending.remove();
	    soundPos=pending.remove();
	    soundVolume=pendingVolumes.remove();
	    addTickToBuffer(theSound, soundVolume, soundPos, buffer, START_OF_BUFFER, bufferSize);
	}
    }


    public synchronized void makeNextBuffer(
    			SoundBuffer buffer, int numberOfFrames, int startPos) {
    // usually, numberOfFrames will equal the size of the buffer,
    // but it might sometimes be useful to fill just part of a buffer?
    
    // startPos: need to be able to start in the middle, to handle
    // branches that begin at odd times
	int bufferPos=startPos, bufferSize = numberOfFrames;
	MMEvent nextSound;
	int whichSound;

	if (bufferPos == 0) {
	    doPending(buffer, bufferSize);
	}
	// if bufferPos !=0 we've just started a new branch,
	// so there won't be anything pending

	for (ScriptPlayer branch : branchList) {
	    branch.makeNextBuffer(buffer, numberOfFrames, startPos);
	}

	for (int i=0; i<= branchList.size()-1; i++) {
	    if (!branchList.get(i).isPlaying()) {
		branchList.remove(i);
//System.out.println("  Removing branch: number of branches is "+branchList.size());
	    }
	}

	while ((!endOfTrack) && (bufferPos<numberOfFrames)) {
	    if (framesAfterTick>0) {
		bufferPos+=(framesPerTick-framesAfterTick);
		framesAfterTick=0;
		tickNumber++;
	    }
	    if (bufferPos<numberOfFrames) {
		nextSound = advanceToNextSound(buffer, numberOfFrames, bufferPos);
		// need to pass bufferPos so that
		// new branches start in the right place
		if (nextSound.getType() != END_OF_TRACK_EVENT) {
		    whichSound = ((SoundEvent)nextSound).getSound();
		    addTickToBuffer(whichSound, currentVolume, START_OF_BUFFER,
		    	buffer, bufferPos, bufferSize);
		    tickNumber++;
		    bufferPos+=framesPerTick;
		    framePosition+=framesPerTick;
		}
	    }
	} //end while
	if (bufferPos>bufferSize) {
	    tickNumber--;
	    int backtrack = bufferPos-bufferSize;
	    framesAfterTick=framesPerTick-backtrack;
	    framePosition -= backtrack;
	}

	if (endOfTrack && (pending.size() == 0) && branchList.isEmpty()) {
	    playing = false;
	}
    } // end makeNextBuffer


    /** Frame position is a 'long' not an 'int' because at 44100 frames
     *  per second, ints will overflow after about 12 hours, whereas
     *  longs are good for about 6 million years.
     */
    public void setFramePosition(long framePos) {
	int nextEventType;
	long framesToGo = framePos - initialFramePosition;

	if (verbose) {System.out.println("Setting frame position "+framePos);}
	if (framesToGo < 0) {error("trying to set a negative frame position");}

	if (framesToGo == 0) {
	    return;
	}
	framesToGo -= 1;

	if (branchDepth == 0) {
	    reset();	// don't need to reset branches because they're newly created;
	    		// in fact, we mustn't reset the branches, because it
			// would send them all back to the default tempo!
	}
	setSeeking(true);
	while (true) {
	    nextEventType = advanceToNextSound().getType();
	    if (nextEventType == END_OF_TRACK_EVENT) {
		break;
	    }
	    tickNumber++;
	    if (framesPerTick > framesToGo) {
		framePosition += framesToGo;
		framesAfterTick = (int)framesToGo;
		break;
	    }
	    framePosition += framesPerTick;
	    framesToGo -= framesPerTick;
	}
	// rewind();  // not sure whether this is needed
	for (ScriptPlayer branch : branchList) {
	    if (verbose) {System.out.println("setFramePosition: descending into branch");}
	    branch.setFramePosition(framePos);
	}
	setSeeking(false);
    } // end setFramePosition


    public long getFramePosition() {
	return framePosition; // not useful at the moment...
    }


    public void setTickPosition(int tickPos) {
	//not yet implemented
    }


    /** returns the frame position of the marker, or
     *  MARKER_NOT_FOUND if the marker doesn't exist.
     *  This value is used for making sure that branches
     *  all start at the right time.
     */
    private long findMarkerPosition(String markerName) {
	long branchPosition=MARKER_NOT_FOUND;

	if (eventList.markerExists(markerName)) {
	    reset();
	    markerToFind = markerName;
	    lookingForMarker = true;
	    setSeeking(true);
	    while (lookingForMarker) {
		advanceToNextSound();
		framePosition += framesPerTick;
	    }
	    framePosition -= framesPerTick; // we haven't played this sound yet!
	    setSeeking(false);
	    return framePosition;
	} else {
	    createBranches();
	    for (ScriptPlayer branch : branchList) {
		branchPosition = branch.findMarkerPosition(markerName);
		if (branchPosition != MARKER_NOT_FOUND) {break;};
	    }
	    return branchPosition;
	}
    }


    private void createBranches() {
	// step through script creating branches, until end of track or infinite repeat
	// used for finding markers in branches
	int eventType;
	MMEvent nextEvent;
	makingBranches = true;
	do {
	    nextEvent = advanceToNextSound();
	    eventType = nextEvent.getType();
// System.out.println("got event "+nextEvent);
	} while ((eventType != END_OF_TRACK_EVENT) && (eventType != REPEAT_END_EVENT));
	makingBranches = false;
    }


    private void rewind() { // nb this will only go back one event
	rewinding = true;
    }


    public long startFromMarker(String markerName) {
    // return the frame position of the marker, or -1 if not found
	long markerPosition = findMarkerPosition(markerName);
	if (markerPosition!=MARKER_NOT_FOUND) {
	    if (verbose) {System.out.println("Found marker:");}
	    setFramePosition(markerPosition);
	    return markerPosition;
	}
	else {
	    if (verbose) {System.out.println("marker not found");}
	    return -1;
	}
    }


    public void reset() {
	endOfTrack = false;
	setTempo(DEFAULT_TEMPO);
	tempoStack.clear();
	accelMode = NO_ACCEL_MODE;
	tickNumber = 0;
	framesAfterTick = 0;
	framePosition = 0;
	pending.clear();
	pendingVolumes.clear();
	branchList.clear();
	// branchFramePositions.clear();
	while (!tempoStack.empty()) {tempoStack.pop();}
	eventList.resetPosition();
	setSeeking(false);
	lookingForMarker = false;
	makingBranches = false;
	rewinding = false;
	gotVolume = false;
    }


    /** If seeking is true, no sound is generated--
     *  useful for starting in the middle of a script.
     */
    public void setSeeking(boolean seeking) {
	this.seeking = seeking;
	for (ScriptPlayer branch : branchList) {branch.setSeeking(seeking);}
    }


    private MMEvent advanceToNextSound(
    			SoundBuffer buffer, int numberOfFrames, int startPos) {
	MMEvent nextEvent;
	int eventType;

	if (rewinding) {
	    rewinding = false;
	    return rememberedEvent;
	}
	lookfornextsound: do {
	    nextEvent = eventList.getNextEvent();
	    eventType = nextEvent.getType();
	    switch (eventType) {
		case ABSOLUTE_TEMPO_EVENT:
		case RELATIVE_TEMPO_EVENT:
			doTempoEvent(nextEvent);
		    break;
		case PUSH_TEMPO_EVENT:
		    tempoStack.push(tempo);
		    break;
		case POP_TEMPO_EVENT:
		    if (!tempoStack.empty()) {
			setTempo(tempoStack.pop());
		    }
		    // trying to pop from an empty stack leaves the tempo unchanged
		    break;
		case ACCEL_START_EVENT:
		    accelMode = ((AccelStartEvent)nextEvent).getMode();
		    doTempoEvent(((AccelStartEvent)nextEvent).getStartTempo());
		    accelStartTempo = tempo;

		    // find out the final tempo of the accelerando...
		    doTempoEvent(((AccelStartEvent)nextEvent).getEndTempo());
		    accelEndTempo = tempo;
		    // ... but restore the correct beginning tempo...
		    setTempo(accelStartTempo);
		    int accelLength = ((AccelStartEvent)nextEvent).getLength();
		    accelFactor = calculateAccelFactor(
				    accelStartTempo, accelEndTempo, accelLength, accelMode);
//System.out.println("Accel from "+accelStartTempo+" to "+accelEndTempo
//	+" over "+accelLength+" ticks:");
//System.out.println("Set accelfactor to "+accelFactor);
		    break;
		case ACCEL_END_EVENT:
		    accelMode = NO_ACCEL_MODE;
		    setTempo(accelEndTempo);
		    break;
		case BRANCH_EVENT:
		    MMEventList newBranch =
			((BranchEvent)nextEvent).getBranch();
		    ScriptPlayer newPlayer = new ScriptPlayer (newBranch, sounds, framePosition);
		    // branchFramePositions.add(framePosition);
		    newPlayer.setTempo(tempo);
		    newPlayer.branchDepth = branchDepth+1;
		    branchList.add(newPlayer);
//System.out.println("Adding branch: number of branches is "+branchList.size());
		    if (seeking) {
			newPlayer.setSeeking(true);
		    } else {
			newPlayer.makeNextBuffer(buffer, numberOfFrames, startPos);
		    }
//System.out.println("made first buffer for new branch");
		    break;
		case MARKER_EVENT:
		    if (lookingForMarker) {
			if (((MarkerEvent)nextEvent).getName().equals(markerToFind)) {
			    lookingForMarker=false;
			    break lookfornextsound;
			}
		    }
		    break;
		case VOLUME_EVENT:
		    gotVolume = true;
		    currentVolume = ((VolumeEvent)nextEvent).getVolume()/DEFAULT_VOLUME;
		    break;
		case END_OF_TRACK_EVENT:
		    endOfTrack = true;
		    break;
		case REPEAT_END_EVENT:
		    if (((RepeatEndEvent)nextEvent).getRepeatsRemaining()
			    == INFINITE_REPEAT) {
			if (lookingForMarker) {
			    error("got infinite repeat while looking for marker "+markerToFind);
			}
			if (makingBranches) {
			    break lookfornextsound;
			}
		    }
		    break;
		default:
		    break;
	    }
	}  // end do
	while ((eventType!=SOUND_EVENT) && (eventType!=END_OF_TRACK_EVENT));

	switch (accelMode) {
	    case EXP_ACCEL_MODE:
		setTempo(tempo*accelFactor);
		break;
	    case LINEAR_ACCEL_MODE:
		setTempo(tempo+accelFactor);
		break;
	    default: // i.e, NO_ACCEL_MODE
		break;
	}
	rememberedEvent = nextEvent;
	if (!gotVolume) {
	    currentVolume = 1.0f;
	}
	gotVolume = false; // ensure that sounds have default volume unless otherwise specified
	if (verbose) {System.out.println(nextEvent);}
	return nextEvent;
    }


    private MMEvent advanceToNextSound() { // use only for seeking
	return advanceToNextSound(null, 0, 0);
    }


    private void doTempoEvent(MMEvent tempoEvent) {
	    if (tempoEvent != null) {
	    // nb null events may happen at the start or end of accel blocks
		int eventType = tempoEvent.getType();
		switch (eventType) {
		    case ABSOLUTE_TEMPO_EVENT:
			float newTempo =
			    ((AbsoluteTempoEvent)tempoEvent).getTempo();
			setTempo(newTempo);
			break;
		    case RELATIVE_TEMPO_EVENT:
			float tempoRatio =
			    ((RelativeTempoEvent)tempoEvent).getTempoRatio();
			setTempo(tempo*tempoRatio);
			break;
		    default:
			error("doTempoEvent: event type "+eventType
				+" is not a tempo event.");
		}
	    }
    }


    private float calculateAccelFactor(float start, float end, int length, float mode) {
	// if (length ==0) {error("accel block has zero length.");}
	if (length==0) {return 1;}
	if (mode == LINEAR_ACCEL_MODE) {
	    return (end-start)/length;
	}
	else {
	    return (float)Math.pow(end/start, 1.0f/length);
	}
    }


    private void error (String message) {
        System.out.println("Error in class ScriptPlayer: "
                            + message);
        System.exit(1);
    }

    public void run() {
	playing = true;
	paused = false;
	buffer=new SoundBuffer(sounds.getAudioFormat(), bufferSize);
	SourceDataLine line = Tickers.openAudioLine(sounds.getAudioFormat());
	line.start();
/* hack: sometimes the first sound doesn't come out cleanly,
 * so we play some empty buffers first */
	line.drain();
	buffer.clearBuffer();
	for (int i=0; i<10; i++) { buffer.playBuffer(line); }

	while (playing) {
	    if (paused) {
		try {Thread.sleep(100);}
		 catch (InterruptedException e) {/*do nothing*/}
	    } else {
		buffer.clearBuffer();
		makeNextBuffer(buffer,bufferSize,START_OF_BUFFER);
		buffer.playBuffer(line);
		MMgui.updateTime();
	    }
	}  // end while playing
	line.drain();
	line.close();
	playing = false;
	reset();
	MMgui.reachedEndOfScript();
    }


    public void stopPlaying() {
	playing = false;
    }


    public void pausePlaying() {
	paused = true;
    }


    public void resumePlaying() {
	paused = false;
    }

}
