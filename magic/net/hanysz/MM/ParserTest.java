package net.hanysz.MM;

import net.hanysz.MM.events.*;
import net.hanysz.MM.parser.*;
import net.hanysz.MM.audio.*;
import javax.sound.sampled.*;

class ParserTest implements net.hanysz.MM.MMConstants {

    static String[] soundnames = {"tick", "bell", "crash", "silly"};
    static String[] soundFilenames =
        {"sounds/tick.wav",  // nb pathnames are relative to the openAudioFile method
        "sounds/bell.wav",
        "sounds/crash.wav",
        "sounds/silly.wav"};


    public static void main(String[] args)
    throws ParseException, TokenMgrError {
	MMEventList eventList = new MMEventList();
	java.io.StringReader sr = new java.io.StringReader(args[0]);
	java.io.Reader r = new java.io.BufferedReader(sr);
	MMScriptParser parser = new MMScriptParser(r);
	SoundCollection sounds;
	ScriptPlayer scriptPlayer;
	SoundBuffer buffer;
	int bufferSize = 1000;
	int i = 0;

	parser.Start(eventList);

	System.out.println("Finished parsing.  Here is the event list.");
	System.out.println(eventList);

/*
	System.out.println("\nPlaying...");
	int i=0;
	MMEvent nextEvent;
	do {
	    nextEvent = eventList.getNextEvent();
	    System.out.println(nextEvent);
	}
	while (i++<20 && nextEvent.getType() != END_OF_TRACK_EVENT);
*/


        sounds=new SoundCollection(44100, 16);
        for (i=0; i<soundFilenames.length; i++) {
            java.net.URL fileURL = SoundCollection.class.getResource(soundFilenames[i]);
            sounds.openURL(fileURL, soundnames[i]);
        }

	scriptPlayer = new ScriptPlayer(eventList,sounds);
	System.out.println("SoundCollection and ScriptPlayer successfully initialised.");

	SourceDataLine line = Tickers.openAudioLine(sounds.getAudioFormat());
	line.start();
	System.out.println("Audio line successfully opened.");

	buffer=new SoundBuffer(sounds.getAudioFormat(), bufferSize);

	i = 1;
	System.out.println("Playing...");
	while (scriptPlayer.isPlaying()) {
	    buffer.clearBuffer();
	    scriptPlayer.makeNextBuffer(buffer,bufferSize, START_OF_BUFFER);
	    // System.out.println("Made buffer number "+i++);
	    buffer.playBuffer(line);

	}

    }
}
