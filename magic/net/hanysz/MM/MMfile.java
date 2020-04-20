package net.hanysz.MM;

/* MM.java hacked to write output to a .au file */
/* to keep it simple, we'll hardcode the filename as "click.au" */

import net.hanysz.MM.events.*;
import net.hanysz.MM.parser.*;
import net.hanysz.MM.audio.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class MMfile implements net.hanysz.MM.MMConstants {

    /*
    static String[] soundnames = {"tick", "bell", "crash", "silly"};
    static String[] soundFilenames =
        {"sounds/tick.wav",  // nb pathnames are relative to the openAudioFile method
        "sounds/bell.wav",
        "sounds/crash.wav",
        "sounds/silly.wav"};
    */
    public static boolean verbose = false;
    static boolean useGui = true;
    static boolean scriptFromFile = false;
    static boolean scriptFromStdin = false;
    static boolean startFromMarker = false;
    static boolean useCountin = false;
    static String markerName;
    static boolean startFromTime = false;
    static long startTime;
    static boolean startFromTick = false;
    static int startTick;
    static boolean parseOnly = false;
    static int bufferSize = 1000;

    static StringBuilder theScript = new StringBuilder();
    static StringBuilder countinScript = new StringBuilder();
    // static StringBuilder modifiableScript;
    static MMEventList eventList = new MMEventList();
    static MMEventList countinList = new MMEventList();
    static SoundCollection sounds;
    static ScriptPlayer scriptPlayer, countinPlayer;
    static SoundBuffer buffer;

    static FileOutputStream outfile;


    // problem: sounds are not loaded in the right order!
    static void loadBuiltInSounds() {
	/*
	for (int i=0; i<soundFilenames.length; i++) {
	    java.net.URL fileURL = SoundCollection.class.getResource(soundFilenames[i]);
	    sounds.openURL(fileURL, soundnames[i]);
	}
	*/
	Properties soundFileNames = new Properties();
	java.net.URL soundList = MM.class.getResource("audio/sounds/contents.txt");
	/*
	try {
	    soundFileNames.load(soundList.openStream());
	} catch (IOException e) {
	    System.out.println("Error loading built-in sounds.");
	    System.exit(1);
	}
	for (Enumeration e = soundFileNames.propertyNames() ; e.hasMoreElements() ;) {
	    String soundName = (String)e.nextElement();
	    String soundFileName = soundFileNames.getProperty(soundName);
	    java.net.URL fileURL = SoundCollection.class.getResource("sounds/"+soundFileName);
	    sounds.openURL(fileURL, soundName);
	}
	*/
	Scanner sc = null;
	try {
	    sc = new Scanner(soundList.openStream());
	} catch (IOException e) {
	    System.out.println("Error loading built-in sounds.");
	    System.exit(1);
	}
	while (sc.hasNext()) {
	    String soundName = sc.next();
	    String soundFileName = sc.next();
	    java.net.URL fileURL = SoundCollection.class.getResource("sounds/"+soundFileName);
	    sounds.openURL(fileURL, soundName);
	}
    }


    static void processCommandLineArguments(String[] args) {
    // basic version, doesn't handle errors yet
	int i=0;
	while (i < args.length) {
	    useGui = false; // use gui only if there are no command line arguments
	    if (args[i].equals("-f")) {
		i++;
		String fileName = args[i];
		File file = new File(fileName);
		StringBuilder fileContents = new StringBuilder();
		String line;
		try {
		    BufferedReader in = new BufferedReader(new FileReader(fileName));
		    while ((line = in.readLine()) != null) {
			fileContents.append(line+"\n");
		    }
		    in.close();
		}
		catch (IOException e) {
		    System.out.println("Error reading from file:");
		    e.printStackTrace();
		    System.exit(1);
		}
		theScript.append(fileContents.toString());
		scriptFromFile = true;
	    } // end if reading from file
	    else if (args[i].equals("-v")) {
		verbose = true;
	    }
	    else if (args[i].equals("-p")) {
		parseOnly = true;
	    }
	    else if (args[i].equals("-b")) {
		i++;
		bufferSize = Integer.parseInt(args[i]);
	    }
	    else if (args[i].equals("-m")) {
		startFromMarker = true;
		i++;
		markerName = args[i];
	    }
	    else if (args[i].equals("-c")) {
		useCountin = true;
		i++;
		countinScript.append(args[i]);
		System.out.println("Got countin script!");
	    }
	    else if (args[i].equals("-t")) {
		startFromTime = true;
		i++;
		startTime = Long.parseLong(args[i]);
	    }
	    else if (args[i].equals("-n")) {
		startFromTick = true;
	    }
	    else if (args[i].equals("-")) {
		System.out.println("Reading script from standard input...");
		StringBuilder standardInput = new StringBuilder();
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
		    standardInput.append(scanner.nextLine());
		}
		theScript.append(standardInput.toString());
	    }
	    else {
		theScript.append(args[i]);
	    }
	    i++;
	} // end while

	// nb if multiple scripts are specified, they are concatenated--
	// this is a feature not a bug!

	if (verbose) {
	    System.out.println("Script is:\n"+theScript+"\n");
	}
    }


    public static void main(String[] args)
    throws ParseException, TokenMgrError, InterruptedException, IOException {
	processCommandLineArguments(args);
	if (useGui) {
	    // MMgui.main(args);
	    sounds=new SoundCollection(DEFAULT_SAMPLE_RATE, DEFAULT_SAMPLE_SIZE);
	    loadBuiltInSounds();
	    MMgui.makeGUI(sounds, bufferSize);
	}
	else {

	    /*
	    modifiableScript = new StringBuilder(theScript);
	    MMScriptParser.preProcess(modifiableScript);
	    theScript = modifiableScript.toString();
	    */

	    // MMScriptParser.preProcess(theScript);
	    // MMPreProcessor mmPreProcessor 
	    new MMPreProcessor().preProcess(theScript);

	    if (verbose) {
		System.out.println("Script after preprocessing:");
		System.out.println(theScript);
		System.out.println("\n");
	    }

	    java.io.StringReader sr = new java.io.StringReader(theScript.toString());
	    java.io.Reader r = new java.io.BufferedReader(sr);

	    MMScriptParser parser = new MMScriptParser(r);
	    int length = parser.Start(eventList);

	    if (verbose) {
		System.out.println("Finished parsing.  Number of ticks is "
		    + length + ". Here is the event list.");
		System.out.println(eventList);
		System.out.println("\n");
	    }

	    // MMScriptParser.postProcess(eventList); // -- not needed?

	    if (parseOnly) return;

	    sounds=new SoundCollection(DEFAULT_SAMPLE_RATE, DEFAULT_SAMPLE_SIZE);
	    loadBuiltInSounds();
	    buffer=new SoundBuffer(sounds.getAudioFormat(), bufferSize);

	    scriptPlayer = new ScriptPlayer(eventList,sounds);

/* to do: open a file for audio output */
	    try {
		outfile = new FileOutputStream("click.au");
		
		byte[] fileHeader = {46, 115, 110, 100, // header ".snd"
		0,0,0,28, // offset: sound data starts at byte 28
		-1,-1,-1,-1, // unknown file length
		0,0,0,3, // 16 bit linear encoding
		0,0,-84,68, // 172*256+68 = 441000 hz sample rate
		0,0,0,1, // 1 channel
		0,0,0,0}; // blank "text comment"
		outfile.write(fileHeader);
	    } finally {};

	    if (startFromMarker) {
		scriptPlayer.startFromMarker(markerName);
	    }
	    else if (startFromTime) {
		startTime = (startTime*DEFAULT_SAMPLE_RATE)/1000;
		scriptPlayer.setFramePosition(startTime);
	    }
	    else if (startFromTick) {
		System.out.println("sorry, start from tick is not yet implemented.");
	    }
	    if (useCountin) {
	        new MMPreProcessor().preProcess(countinScript);
		sr = new java.io.StringReader(countinScript.toString());
		r = new java.io.BufferedReader(sr);

		parser = new MMScriptParser(r);
		length = parser.Start(countinList);
		countinPlayer = new ScriptPlayer(countinList,sounds);

		System.out.println("Playing countin...");
		while (countinPlayer.isPlaying()) {
		    buffer.clearBuffer();
		    countinPlayer.makeNextBuffer(buffer,bufferSize,START_OF_BUFFER);
		    try {buffer.writeBuffer(outfile); } finally {};
		}  // end while playing
	    }
	    System.out.println("Playing...");
	    while (scriptPlayer.isPlaying()) {
		buffer.clearBuffer();
		scriptPlayer.makeNextBuffer(buffer,bufferSize,START_OF_BUFFER);
		try { buffer.writeBuffer(outfile); } finally {}
	    }  // end while playing
	    try { outfile.close(); } finally {};
	} // end else (play script)
    } // end main
} // end class
