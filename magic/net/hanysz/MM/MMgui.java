package net.hanysz.MM;

/* MMgui: part of the Magic Metronome project
 *
 * Uses SoundCollection, Tickers
 *
 * To do: fetch soundnames from resource bundle
 *
 * public MMgui()
 * private void setUpHelp()
 * private void setUpFileChooser()
 * private void makePanelsAndButtons()
 * private void setUpMenus()
 * private void loadExampleFiles()
 * private static void getStringFromResource(String resourceName) {
 * public void actionPerformed(ActionEvent event)
 * private boolean exampleMenuActivated(Object source)
 * public void hyperlinkUpdate(HyperlinkEvent e)
 * private static void openScript()
 * private static int whichCharacter(int whichLine, int whichCol) {
 * public static void displayErrorMessage(String message, String errorType) {
 * private static void showParsingError()
 * private static void showParsingError(String message) {
 * private static void showParsingError(ParseException e) {
 * private static void parseScript(StringBuilder theScript) {
 * private static void startScriptPlayer(StringBuilder theScript)
 * private void stopScriptPlayer()
 * public static void updateTime()
 * private static void setUpTimer()
 * public static void reachedEndOfScript()
 * private static void createAndShowGUI()
 * public static void makeGUI(SoundCollection theSounds, int theBufferSize)
 *
 */


import net.hanysz.MM.audio.*;
import net.hanysz.MM.events.*;
import net.hanysz.MM.parser.*;
import net.hanysz.MM.misc.*;
import javax.sound.sampled.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
// import java.io.IOException;


public class MMgui implements ActionListener, HyperlinkListener, MMConstants {
    static JFrame MMFrame, helpFrame, aboutFrame;
    static JPanel MMPanel, controlPanel, helpPanel, aboutPanel;
    static JEditorPane scriptText, helpText, aboutText;
    static JScrollPane scriptPane;
    static JButton playPauseButton, stopButton, goButton;
    static JTextField timeBox, markerBox;
    static JMenuBar menuBar;
    static JMenu fileMenu, editMenu, examplesMenu, helpMenu;

    static JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
    static final int MAX_VOLUME=(int)DEFAULT_VOLUME*2;

    static java.util.List<String> soundnames = new ArrayList<String>();

    static java.util.List<JMenuItem> exampleMenuItems = new ArrayList<JMenuItem>();
    static java.util.List<java.net.URL> exampleMenuURLs = new ArrayList<java.net.URL>();

    static float sampleRate=44100;
    static int sampleSize=16;
    static int bufferSize=1000;
    static float currentTime=0, startTime=0, timePerBuffer=bufferSize/sampleRate;
    static float actualTime=0;
    // currentTime is the time shown in the box, managed by the timer;
    // actualTime is the time as measured by scriptPlayer;
    // they don't always synchronise!
    static javax.swing.Timer timer;
    static int updatePeriod = 49; //number of milliseconds between time display updates
    static float updatePeriodInSeconds = 0.049f;
    static int timerDelay=5, timerWait; // kludge to try and sync timer with playback
    static NumberFormat timeFormat = new DecimalFormat("0000.000");
    static String markerName;

    static SoundCollection sounds;
    static MMEventList eventList;
    static ScriptPlayer scriptPlayer;
    static SourceDataLine line;

    static int initialWidth=680;
    static int initialHeight=500;
    static int scriptWidth=770;
    static int scriptHeight=400;
    static int helpWidth=615;
    static int helpHeight=580;
    static int aboutWidth=415;
    static int aboutHeight=370;

    static String genericParsingErrorMessage, macroUseMessage;

    static StringBuilder theScript;
    static boolean macrosUsed=false;
    static boolean playing=false, paused=false;
    static String currentFilename = null;
    static boolean changedSinceSave=false, changedSincePlay=false;


    public MMgui() {
	MMFrame = new JFrame("Magic Metronome version 0.40 alpha");
	MMFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	MMPanel = new JPanel();
	// MMPanel.setLayout(new BoxLayout(MMPanel, BoxLayout.PAGE_AXIS));
	MMPanel.setLayout(new BorderLayout());

	controlPanel=new JPanel();
	// controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
	// actually box layout doesn't seem to gain anything over flow layout...

	setUpMenus(); // nb this must come before makePanelsAndButtons
	makePanelsAndButtons();
	setUpHelp();
	setUpFileChooser();

	MMPanel.add(controlPanel,BorderLayout.PAGE_START);
	MMPanel.add(scriptPane,BorderLayout.CENTER);

	MMFrame.getContentPane().add(MMPanel);

	MMFrame.pack();
	MMFrame.setSize(new Dimension(initialWidth, initialHeight));
	scriptText.setCaretPosition(scriptText.getText().length());
	scriptText.requestFocusInWindow();
	MMFrame.setVisible(true);
    }


    private void setUpHelp() {
        helpFrame = new JFrame("Magic Metronome Instructions");
        helpFrame.setSize(new Dimension(helpWidth, helpHeight));
        helpText = new JEditorPane();
        helpText.setEditable(false);
	setHelpToContentsPage();
        helpText.addHyperlinkListener(this);

        JScrollPane helpScrollPane = new JScrollPane(helpText);
        helpFrame.add(helpScrollPane);

	aboutFrame = new JFrame("About Magic Metronome");
        aboutFrame.setSize(new Dimension(aboutWidth, aboutHeight));
        aboutText = new JEditorPane();
        aboutText.setEditable(false);
        aboutText.addHyperlinkListener(this);

	java.net.URL aboutURL = MMgui.class.getResource("help/about.html");
	try {
	    aboutText.setPage(aboutURL);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(MMFrame,
		"Error in setUpHelp routine: Attempted to read a bad URL: " + aboutURL,
		"Initialisation error", JOptionPane.ERROR_MESSAGE);
	}

        JScrollPane aboutScrollPane = new JScrollPane(aboutText);
        aboutFrame.add(aboutScrollPane);
    } // end setUpHelp


    private void setHelpToContentsPage() {
	java.net.URL helpURL = MMgui.class.getResource("help/contents.html");
	try {
	    helpText.setPage(helpURL);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(MMFrame,
		"Error in setUpHelp routine: Attempted to read a bad URL: " + helpURL,
		"Initialisation error", JOptionPane.ERROR_MESSAGE);
	}
    }


    private void setUpFileChooser() {
	fileChooser.addChoosableFileFilter(new TextFileFilter());
    }


    private void makePanelsAndButtons() {
	JLabel timeLabel = new JLabel("Time: ");
	controlPanel.add(timeLabel);
	timeBox = new JTextField("0000.000",8); // 8 columns, not resizable
	controlPanel.add(timeBox);
	controlPanel.add(Box.createRigidArea(new Dimension(25, 0)));

	playPauseButton = new JButton("Play");
	stopButton = new JButton("Stop");
	controlPanel.add(playPauseButton);
	controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
	controlPanel.add(stopButton);

	controlPanel.add(Box.createRigidArea(new Dimension(25, 0)));
	JLabel markerLabel = new JLabel("Marker: ");
	controlPanel.add(markerLabel);
	markerBox = new JTextField(12); // 12 columns, not resizable
	controlPanel.add(markerBox);
	controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
	goButton = new JButton("Go");
	controlPanel.add(goButton);
	controlPanel.add(Box.createRigidArea(new Dimension(100, 0)));

	playPauseButton.addActionListener(this);
	stopButton.addActionListener(this);
	goButton.addActionListener(this);

        scriptText = new JEditorPane();
	java.net.URL welcomeURL = MMgui.class.getResource("examples/welcome_message.txt");
	try {
	    scriptText.setPage(welcomeURL);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(MMFrame,
		"Error in makePanelsAndButtons routine: Attempted to read a bad URL: "
		+ welcomeURL,
		"Initialisation error", JOptionPane.ERROR_MESSAGE);
	}
	
	scriptPane = new JScrollPane(scriptText);
	scriptPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scriptPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	// scriptPane.setPreferredSize(new Dimension(scriptWidth, scriptHeight));
    } // end makePanelsAndButtons


    private void setUpMenus() {
	JMenuItem menuItem;

	fileMenu = new JMenu("File");
	menuItem = new JMenuItem("Open");
	menuItem.addActionListener(this);
	fileMenu.add(menuItem);
	menuItem = new JMenuItem("Save");
	menuItem.addActionListener(this);
	fileMenu.add(menuItem);
	menuItem = new JMenuItem("Save as");
	menuItem.addActionListener(this);
	fileMenu.add(menuItem);

	editMenu = new JMenu("Edit");
	// menuItem = new JMenuItem("Undo");
	// editMenu.add(menuItem);
	// menuItem = new JMenuItem("Redo");
	// editMenu.add(menuItem);
	// editMenu.addSeparator();
	menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
        menuItem.setMnemonic(KeyEvent.VK_T); // not sure what this does, just copied it from elsewhere
	editMenu.add(menuItem);
        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(menuItem);
        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("Paste");
        menuItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(menuItem);

	examplesMenu = new JMenu("Examples");
	loadExampleFiles();

	helpMenu = new JMenu("Help");
	menuItem = new JMenuItem("Help");
	helpMenu.add(menuItem);
	menuItem.addActionListener(this);
	menuItem = new JMenuItem("About");
	menuItem.addActionListener(this);
	helpMenu.add(menuItem);

	menuBar = new JMenuBar();
	menuBar.add(fileMenu);
	menuBar.add(editMenu);
	menuBar.add(examplesMenu);
	menuBar.add(helpMenu);

	MMFrame.setJMenuBar(menuBar);
    } // end setUpMenus


    private void loadExampleFiles() {
	String exampleName, fileName;
	JMenuItem menuItem;
	JMenu subMenu = null; // compiler insists it must be initialised!
	boolean madeSubmenu=false;
	java.net.URL exampleURL;

	exampleURL = MM.class.getResource("examples/contents.txt");
	Scanner sc = null;
        try {
            sc = new Scanner(exampleURL.openStream());
        } catch (IOException e) {
            System.out.println("Error loading examples files.");
            System.exit(1);
        }
        while (sc.hasNext()) {
            exampleName = sc.next();
	    if (exampleName.charAt(0)!='#') { // ignore comment lines
		fileName = sc.next();
		exampleURL = MM.class.getResource(fileName);
		String newString = exampleName.replace('_',' ');
		if (fileName.equals("MENU")) { // to be implemented: make submenu
		    subMenu = new JMenu(newString);
		    examplesMenu.add(subMenu);
		    madeSubmenu=true;
		} else {
		    menuItem = new JMenuItem(newString);
		    menuItem.addActionListener(this);
		    if (madeSubmenu) {
			subMenu.add(menuItem);
		    } else {
			examplesMenu.add(menuItem);
		    }
		    exampleMenuItems.add(menuItem);
		    exampleMenuURLs.add(MM.class.getResource("examples/"+fileName));
		}
	    } // end if not comment line
	    else { sc.skip(Pattern.compile(".*\n"));}
        }
    } // end loadExampleFiles


    private static String getStringFromResource(String resourceName) {
	StringBuilder tempString = new StringBuilder();

	java.net.URL resourceURL = MMgui.class.getResource(resourceName);
	// Now getting the text out of the URL is surprisingly complicated!
	try {
	    BufferedReader dis =
	      new BufferedReader(new InputStreamReader(new BufferedInputStream(resourceURL.openStream())));
	    String s;
	    while ((s = dis.readLine()) != null) {
		tempString.append(s);
		tempString.append("\n");
	     }
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(MMFrame,
		"Error loading resource: Attempted to read a bad URL: " + resourceURL,
		"Resource load error", JOptionPane.ERROR_MESSAGE);
	}
	return tempString.toString();
    }


    public void actionPerformed(ActionEvent event) {
	Object source = event.getSource();
	String command = event.getActionCommand();

	if (exampleMenuActivated(source)) { return; } // this has the side effect of loading the example
	if (source==playPauseButton) {
	    if (!playing) {
		theScript=new StringBuilder(scriptText.getText());
		startScriptPlayer(theScript);
		 // playPauseButton.setText("Pause"); // not needed: this is done by startScriptPlayer
	    } else if (!paused) {
		scriptPlayer.pausePlaying();
		paused=true;
		timeBox.setText(timeFormat.format(actualTime));
		playPauseButton.setText("Resume");
	    } else { // playing but paused
		currentTime = actualTime;
		setUpTimer();
		timer.start();
		scriptPlayer.resumePlaying();
		paused=false;
		playPauseButton.setText("Pause");
	    }
	}
	if (source==stopButton) {
	    if (playing) {stopScriptPlayer();}
	}
	if (source==goButton) { // i.e. start from a marker
	    if (!playing | paused) {
		markerName = markerBox.getText();
		if ((markerName==null) | (markerName.length()==0)) {
		    displayErrorMessage("You need to type a marker name "
			+"into the 'Marker:' box!",
			"No marker set");
		} else {
		    theScript=new StringBuilder(scriptText.getText());
		    parseScript(theScript);
		    long markerFrame = scriptPlayer.startFromMarker(markerName);
		    if (markerFrame<0) {
			displayErrorMessage("The marker '"+markerName
			    +"' does not appear in your script. \n"
			    +"You can add a marker to your script by typing "
			    +"M\"name\"",
			    "Marker not found");
		    } else {
			currentTime = markerFrame/sampleRate;
			actualTime = currentTime;
			timeBox.setText(timeFormat.format(currentTime));
			playing = true;
			paused = false;
			playPauseButton.setText("Pause");
			setUpTimer();
			timer.start();
			scriptPlayer.start();
		    }
		} 
	    } // end if not playing
	} // end goButton
	if (command.equals("Help")) {
	    setHelpToContentsPage();
	    helpFrame.setVisible(true);
	}
	if (command.equals("About")) {
	    aboutFrame.setVisible(true);
	}
	if (command.equals("Open")) {
	    openScript();
	}
	if (command.equals("Save")) {
	    saveScript(currentFilename);
	}
	if (command.equals("Save as")) {
	    saveScriptAs();
	}
    } // end "actionPerformed"


    private boolean exampleMenuActivated(Object source) {
    // check if source is an example menu item:
    // if so, load the corresponding example file and return true
	    for (int i=0; i<exampleMenuItems.size(); i++) {
		if (source==exampleMenuItems.get(i)) {
		    try {
			scriptText.setPage(exampleMenuURLs.get(i));
			currentFilename=null;
		    } catch (IOException e) {
			JOptionPane.showMessageDialog(MMFrame,
			    "Error loading example file: Attempted to read a bad URL: "
			    + exampleMenuURLs.get(i),
			    "Example load error", JOptionPane.ERROR_MESSAGE);
			// this should never happen!
		    }
		    return true;
		}
	}
	return false;
    }


    public void hyperlinkUpdate(HyperlinkEvent e) {
    // triggered by clicking on links in help window
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                helpText.setPage(e.getURL());
            } catch(IOException ioe) {
		    JOptionPane.showMessageDialog(MMFrame,
			"Error following link: Attempted to read a bad URL: " + e.getURL(),
			"Hyperlink error", JOptionPane.ERROR_MESSAGE);
	    }
	}
    }


    private static void openScript() {
	if(fileChooser.showOpenDialog(MMFrame)==JFileChooser.APPROVE_OPTION) {
	    //    readInFile(dialog.getSelectedFile().getAbsolutePath());
	    String fileName=fileChooser.getSelectedFile().getAbsolutePath();
	    try {
		FileReader r = new FileReader(fileName);
		scriptText.read(r,null);
		currentFilename=fileName;
	    } catch(IOException e) {
		JOptionPane.showMessageDialog(MMFrame,
		    "Error loading file "+fileName,
		    "Open error", JOptionPane.ERROR_MESSAGE);
	    } // end try/catch
	} // end if approved choice
    } //end openScript


    private static void saveScript(String fileName) {
	if (fileName==null) {saveScriptAs(); return;}
	try {
	    File f = new File(fileName);
	    if (f.exists()) {
		if (JOptionPane.showConfirmDialog(MMFrame,
			"The file "+fileName+ " already exists."
			   + " Do you want to overwrite it?",
			"File already exists", JOptionPane.YES_NO_OPTION, 
			JOptionPane.WARNING_MESSAGE)
		    == JOptionPane.NO_OPTION) { return; }
	    }
	    FileWriter w = new FileWriter(f);
		scriptText.write(w);
		w.close();
		changedSinceSave = false;
		currentFilename = fileName;
	} catch(IOException e) {
	JOptionPane.showMessageDialog(MMFrame,
	    "Error saving file "+fileName,
	    "Save error", JOptionPane.ERROR_MESSAGE);
	}
    }


    private static void saveScriptAs() {
	if (currentFilename==null) {
	    fileChooser.setSelectedFile(new File("untitledMMscript.txt"));
	}
	if(fileChooser.showSaveDialog(MMFrame)==JFileChooser.APPROVE_OPTION) {
		saveScript(fileChooser.getSelectedFile().getAbsolutePath());
	}
    }


    private static int whichCharacter(int whichLine, int whichCol) {
    	// find the character of the script at the given position
	// Annoyingly, ParseExceptions give the line and column
	// rather than the position counting from the beginning
	// Return -1 if desired character is out of bounds
	if (whichLine<0 | whichCol<0) {return -1;}
	String textString=scriptText.getText();
	int i=1, charPos=0, textLength=textString.length();
	while (i < whichLine) {
	    if (charPos>textLength) {return -1;}
	    if (textString.charAt(charPos)=='\n') {i++;}
	    charPos++;
	}
	charPos = charPos + whichCol - 1; // nb count both lines and columns from 1 not 0
	if (charPos>textLength) {return -1;}
	scriptText.select(charPos,charPos+1);
	return charPos;
    }


    private static void displayErrorMessage(String message, String errorType) {
	    JOptionPane.showMessageDialog(MMFrame, message,
		 errorType, JOptionPane.ERROR_MESSAGE);
    }


    private static void showParsingError(String message) {
	if (macrosUsed) {message += macroUseMessage;}
	displayErrorMessage(message, "Parsing error");
       }


    private static void showParsingError() {
       showParsingError(genericParsingErrorMessage);
    }


    private static void showParsingError(ParseException e) {
	if (e.tokenImage!=null) {  // i.e. if it's an automatically generated error
	    showParsingError(genericParsingErrorMessage);
	    // we don't want to show the stack trace,
	    // it will only frighten the user!
	    if (!macrosUsed) {
		int charPos=whichCharacter(e.currentToken.beginLine, e.currentToken.beginColumn);
		scriptText.select(charPos,charPos+1);
	    }
	    return;
	} else {
	    showParsingError(e.getMessage());
	}
    }


    private static boolean  parseScript(StringBuilder theScript) {
	eventList = new MMEventList();
	new MMPreProcessor().resetPreProcessor();
	macrosUsed=false;
	try {
	    macrosUsed=new MMPreProcessor().preProcess(theScript);
	    SyntaxChecker.checkScript(theScript.toString());
	} catch (ParseException e) {
	    showParsingError(e);
	    return false;
	} catch (SyntaxException e) {
	    displayErrorMessage(e.getMessage(), "Syntax error");
	    return false;
	}
	java.io.StringReader sr = new java.io.StringReader(theScript.toString());
	java.io.Reader r = new java.io.BufferedReader(sr);

	MMScriptParser parser = new MMScriptParser(r);
	try {
	    int length = parser.Start(eventList);
	} catch (ParseException e) {
	    showParsingError(e);
	    return false;
	} catch (TokenMgrError e) {
	    showParsingError();
	    if (!macrosUsed) {
		int charPos=whichCharacter(e.getLine(),e.getCol());
		scriptText.select(charPos,charPos+1);
	    }
	    return false;
	}

	scriptPlayer = new ScriptPlayer(eventList,sounds,bufferSize,0);
	return true;
    }

    private static void startScriptPlayer(StringBuilder theScript) {
/* the following is a temporary hack:
 * occasionally the first sound doesn't come out,
 * so we cheat by inserting a third of a second of silence
 */
/*
	eventList.add(new AbsoluteTempoEvent(180));
	eventList.add(new SoundEvent(SILENT_SOUND));
	eventList.add(new AbsoluteTempoEvent(DEFAULT_TEMPO));
*/
/* end ugly hack */
/* Ultimately fixed by asking the ScriptPlayer object to play some empty buffers! */
	if (!parseScript(theScript)) { return; }

	playing = true;
	paused = false;
	playPauseButton.setText("Pause");

        try {
            currentTime = Float.parseFloat(timeBox.getText());
        } catch (NumberFormatException e) {
            currentTime = 0;
        }
	if (currentTime < 0) {currentTime=0;}
	actualTime = currentTime;
	scriptPlayer.setFramePosition((long)(currentTime*sampleRate));
	scriptPlayer.start();
	setUpTimer();
	timer.start();
    } // end startScriptPlayer


    private void stopScriptPlayer() {
	timer.stop();
	scriptPlayer.stopPlaying();
	currentTime = 0;
	actualTime = 0;
	timeBox.setText("0000.000");
    }


    public static void updateTime() {
	// currentTime += timePerBuffer;
	// timeBox.setText(timeFormat.format(currentTime));
	actualTime += timePerBuffer;
    }


    private static void setUpTimer() {
	timerWait = timerDelay;
        timer = new javax.swing.Timer(updatePeriod, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
		if (timerWait>0) {
		    timerWait--;
		} else if (playing & !paused) {
		    currentTime+=updatePeriodInSeconds;
		    timeBox.setText(timeFormat.format(currentTime));
		} else {
		    timer.stop();
		    // script has stopped
		    // get accurate time from script player
                    currentTime = scriptPlayer.getFramePosition()/sampleRate;
		    // timeBox.setText(timeFormat.format(currentTime));
                }
            } // end actionPerformed
        }); // end args to new Timer
    } // end setUpTimer


    public static void reachedEndOfScript() {
	playing=false;
	paused=false;
	playPauseButton.setText("Play");
	timeBox.setText("0000.000");
    }


    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(false);
	MMgui mmgui = new MMgui();
    }


    public static void makeGUI(SoundCollection theSounds, int theBufferSize) {
	sounds = theSounds;
	bufferSize = theBufferSize;
	sampleRate = sounds.getSampleRate(); // probably not needed, but included for completeness
	sampleSize = sounds.getSampleSizeInBits();

	int numberOfSounds = sounds.getNumberOfSounds();
	for (int i = 0; i < numberOfSounds; i++) {
	    soundnames.add(sounds.getName(i));
	}

	/*
	tempString = new StringBuilder();
	java.net.URL errorURL = MMgui.class.getResource("messages/generic_parsing_error.txt");
	// Now getting the text out of the URL is surprisingly complicated!
	try {
	    BufferedReader dis =
	      new BufferedReader(new InputStreamReader(new BufferedInputStream(errorURL.openStream())));
	    String s;
	    while ((s = dis.readLine()) != null) {
		tempString.append(s);
		tempString.append("\n");
	     }
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(MMFrame,
		"Error loading resource: Attempted to read a bad URL: " + errorURL,
		"Resource load error", JOptionPane.ERROR_MESSAGE);
	}
	genericParsingErrorMessage = tempString.toString();
*/
	genericParsingErrorMessage = getStringFromResource("messages/generic_parsing_error.txt");
	macroUseMessage = getStringFromResource("messages/macros.txt");

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    } // end makeGUI

}
