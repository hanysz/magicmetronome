package net.hanysz.MM;

import net.hanysz.MM.events.*;
import net.hanysz.MM.parser.*;
import net.hanysz.MM.audio.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class MM implements net.hanysz.MM.MMConstants {

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


    static void loadBuiltInSounds() {
	Properties soundFileNames = new Properties(); // not used?
	java.net.URL soundList = MM.class.getResource("audio/sounds/contents.txt");
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
    throws ParseException, TokenMgrError, InterruptedException {
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
	    if (new MMPreProcessor().preProcess(theScript)) {/* nothing*/};
	    // need to put it in an "if" statement because it has a boolean return value!

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
	    SourceDataLine line = Tickers.openAudioLine(sounds.getAudioFormat());
	    line.start();

	    /* hack: sometimes the first sound doesn't come out cleanly,
	     *  * so we play some empty buffers first */
	    line.drain();
	    buffer.clearBuffer();
	    for (int i=0; i<10; i++) { buffer.playBuffer(line); }

	    if (startFromMarker) {
		long dummyvariable = scriptPlayer.startFromMarker(markerName);
	    }
	    else if (startFromTime) {
		if (verbose) {System.out.println("Starting from time "+startTime);}
		startTime = (startTime*DEFAULT_SAMPLE_RATE)/1000;
		scriptPlayer.setFramePosition(startTime);
		if (verbose) {System.out.println("Setting frame position: "+startTime);}
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
		    buffer.playBuffer(line);
		}  // end while playing
	    }
	    System.out.println("Playing...");
	    while (scriptPlayer.isPlaying()) {
		buffer.clearBuffer();
		scriptPlayer.makeNextBuffer(buffer,bufferSize,START_OF_BUFFER);
		buffer.playBuffer(line);
	    }  // end while playing
	    line.drain();
	    line.close();
	} // end else (play script)
    } // end main
} // end class
