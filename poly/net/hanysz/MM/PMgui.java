package net.hanysz.MM;

/* PMgui: part of the Magic Metronome project
 *
 * Uses SoundCollection, Tickers
 *
 */


import net.hanysz.MM.audio.*;
import java.util.*;
import java.io.File;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.IOException;


public class PMgui implements ActionListener, ChangeListener, HyperlinkListener, MMConstants {
    JFrame MMFrame, helpFrame;
    JPanel MMPanel, titlePanel, controlPanelPanel, helpPanel;
    JScrollPane controlPanelPanelPane; // running out of good variable names!
    JLabel welcomeLabel;
    JButton startAllButton, stopAllButton;
    JButton addTrackButton, removeTrackButton;
    JButton helpButton;
    ImageIcon volumeIcon;
    JEditorPane helpText;

    static int numberOfTracks=2;
    static int maxTracksUsed = numberOfTracks;
    java.util.List<JPanel> controlPanels = new ArrayList<JPanel>();
    java.util.List<JPanel> tempoTickStartButtons = new ArrayList<JPanel>();
    java.util.List<JLabel> tempoLabels = new ArrayList<JLabel>();
    java.util.List<JLabel> volumeLabels = new ArrayList<JLabel>();
    java.util.List<JTextField> tempoFields = new ArrayList<JTextField>();
    java.util.List<JButton> startButtons = new ArrayList<JButton>();
    java.util.List<JComboBox> soundChoosers = new ArrayList<JComboBox>();
    java.util.List<JSlider> volumeControls = new ArrayList<JSlider>();
    java.util.List<JSlider> panControls = new ArrayList<JSlider>(); // not used in this version


    static final int MAX_VOLUME=200;
    static final int DEFAULT_VOLUME=100;

    static Vector<String> soundnames = new Vector<String>();
    // should really give initial size and increment for Vector,
    // but performance isn't an issue in this case

    static float sampleRate=44100;
    static int sampleSize=16;
    static int bufferSize=1000;
    static SoundCollection sounds;
    static Tickers tickers;

    static int initialWidth=860;
    static int initialHeight=343;
    static int helpWidth=415;
    static int helpHeight=580;


    public PMgui() {
	MMFrame = new JFrame("Polymetronome version 0.30 alpha");
	MMFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	MMPanel = new JPanel();
	MMPanel.setLayout(new BoxLayout(MMPanel, BoxLayout.PAGE_AXIS));

	titlePanel = new JPanel();
	controlPanelPanel = new JPanel();
	makePanelsAndButtons();
	setUpHelp();

	MMPanel.add(titlePanel);
	controlPanelPanelPane = new JScrollPane(controlPanelPanel);
	MMPanel.add(controlPanelPanelPane);

	MMFrame.getContentPane().add(MMPanel);

	MMFrame.pack();
	MMFrame.setSize(new Dimension(initialWidth, initialHeight));
	MMFrame.setVisible(true);
    }


    private void setUpHelp() {
	helpFrame = new JFrame("Polymetronome Instructions");
	helpFrame.setSize(new Dimension(helpWidth, helpHeight));
	helpText = new JEditorPane();
        helpText.setEditable(false);
	helpText.addHyperlinkListener(this);

	JScrollPane helpScrollPane = new JScrollPane(helpText);
        // helpScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // helpScrollPane.setPreferredSize(new Dimension(250, 145));
        // helpScrollPane.setMinimumSize(new Dimension(10, 10));
	helpFrame.add(helpScrollPane);
    }


    private void makeNewTempoTickStartButtons(JPanel newTempoTickStartButton) {
	newTempoTickStartButton.setLayout(new BoxLayout(
			newTempoTickStartButton, BoxLayout.PAGE_AXIS));
	
	JLabel newTempoLabel = new JLabel("Tempo");
	newTempoLabel.setBorder(BorderFactory.createEmptyBorder(
				20,0,5,0));
	tempoLabels.add(newTempoLabel);
	newTempoTickStartButton.add(newTempoLabel);
	newTempoTickStartButton.add(newTempoLabel);

	JTextField newTempoField = new JTextField(3);
	newTempoField.setText(DEFAULT_TEMPO_STRING);
	tempoFields.add(newTempoField);
	newTempoTickStartButton.add(newTempoField);

	JComboBox newSoundChooser = new JComboBox(soundnames);
	newSoundChooser.setSelectedIndex(0); // start with the first sound loaded
	newSoundChooser.setBorder(BorderFactory.createEmptyBorder(
				20,0,20,0));
	soundChoosers.add(newSoundChooser);
	newTempoTickStartButton.add(newSoundChooser);

	JButton newStartButton = new JButton("Start");
	startButtons.add(newStartButton);
	newTempoTickStartButton.add(newStartButton);

	newStartButton.addActionListener(this);
	newTempoField.addActionListener(this);
	newSoundChooser.addActionListener(this);
    }


    private void makeNewControlPanel(String panelLabel) {
	JPanel newControlPanel = new JPanel();

	// JLabel newVolumeLabel = new JLabel("Volume");
        java.net.URL imageURL = PMgui.class.getResource("images/vol.gif");
	volumeIcon = new ImageIcon(imageURL);
	JLabel newVolumeLabel = new JLabel(volumeIcon);
	volumeLabels.add(newVolumeLabel);

	JPanel newTempoTickStartButton = new JPanel();
	makeNewTempoTickStartButtons(newTempoTickStartButton);
	tempoTickStartButtons.add(newTempoTickStartButton);

	JSlider newVolumeControl = new JSlider(JSlider.VERTICAL, 0, MAX_VOLUME, DEFAULT_VOLUME);
	newVolumeControl.setMajorTickSpacing(20);
	newVolumeControl.setMinorTickSpacing(10);
	newVolumeControl.setPaintTicks(true);
	newVolumeControl.setBorder(BorderFactory.createEmptyBorder(
				0,30,0,10));

	JPanel newVolumeControlBox = new JPanel();
	newVolumeControlBox.setLayout(new BoxLayout(newVolumeControlBox, BoxLayout.PAGE_AXIS));
	newVolumeControlBox.add(newVolumeControl);
	newVolumeControlBox.add(newVolumeLabel);

	volumeControls.add(newVolumeControl);
	newVolumeControl.addChangeListener(this);

	newControlPanel.add(newTempoTickStartButton);
	// newControlPanel.add(newVolumeControl);
	newControlPanel.add(newVolumeControlBox);
	// newControlPanel.add(newVolumeLabel);
	// save some screen space by not labelling volume?

	newControlPanel.setBorder(
		BorderFactory.createTitledBorder(panelLabel));
	controlPanels.add(newControlPanel);
	controlPanelPanel.add(newControlPanel);
    }


    private void makePanelsAndButtons() {
        welcomeLabel = new JLabel("Welcome to the amazing multi-track metronome!");
	startAllButton = new JButton("Start all");
	stopAllButton = new JButton("Stop all");
	addTrackButton = new JButton("Add track");
	removeTrackButton = new JButton("Remove track");
	helpButton = new JButton("Help");

	titlePanel.add(startAllButton);
	titlePanel.add(stopAllButton);
	titlePanel.add(addTrackButton);
	titlePanel.add(removeTrackButton);
	titlePanel.add(helpButton);

	startAllButton.addActionListener(this);
	stopAllButton.addActionListener(this);
	addTrackButton.addActionListener(this);
	removeTrackButton.addActionListener(this);
	helpButton.addActionListener(this);
	// titlePanel.add(welcomeLabel);

	/* no longer used:
	tempoLabels = new JLabel[maxNumberOfTracks];
	volumeLabels = new JLabel[maxNumberOfTracks];
	tempoFields = new JTextField[maxNumberOfTracks];
	startButtons = new JButton[maxNumberOfTracks];
	soundChoosers = new JComboBox[maxNumberOfTracks];
	volumeControls = new JSlider[maxNumberOfTracks];
	*/
	for (int i=1; i<=numberOfTracks; i++) {
	    makeNewControlPanel("Track "+i);
	}
    }


    public void actionPerformed(ActionEvent event) {
	Object source = event.getSource();

	for (int i=0; i<numberOfTracks; i++) {
	    if (source==soundChoosers.get(i)) {
		tickers.setSound(soundChoosers.get(i).getSelectedIndex(),i);
	    }
	    if (source==tempoFields.get(i)) {
		tryToSetTempo(i);
	    }
	    if (source==startButtons.get(i)) {
		if (tickers.isPlaying(i)) {
		    stopTicker(i);
		}
		else {
		    startTicker(i);
		}
	    }
	} // end "for tracks"
	if (source==startAllButton) {
	    for (int i=0; i<numberOfTracks; i++) {
		startTicker(i);
	    }
	}
	if (source==stopAllButton) {
	    for (int i=0; i<numberOfTracks; i++) {
		stopTicker(i);
	    }
	}
	if (source==addTrackButton) {
	    addTrack();
	}
	if (source==removeTrackButton) {
	    removeTrack();
	}
	if (source==helpButton) {
	    java.net.URL helpURL = PMgui.class.getResource("help/intro.html");
	    try {
		helpText.setPage(helpURL);
	    } catch (IOException e) {
		System.err.println("Attempted to read a bad URL: " + helpURL); // this should never happen!
	    }
	    helpFrame.setVisible(true);
	}
    } // end "actionPerformed"


    public void hyperlinkUpdate(HyperlinkEvent e) {
	if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	      try {
		  helpText.setPage(e.getURL());
	      } catch(IOException ioe) {
	    	    System.err.println("Attempted to read a bad URL: " + e.getURL());
	        }
	    }
	}


    public void stateChanged(ChangeEvent event) {
	JSlider source = (JSlider)event.getSource();
	    float volume=source.getValue();
	    volume/=100;
	    for (int i=0; i<numberOfTracks; i++) {
		if (source==volumeControls.get(i)) {
		    tickers.setGain(volume,i);
		}
	    } // end for
    } // end "stateChanged"

    
    public void startTicker(int track) {
	tickers.setSound(soundChoosers.get(track).getSelectedIndex(),track);
	tryToSetTempo(track);
	startButtons.get(track).setText("Stop");
	tickers.startPlaying(track);
    }


    public void stopTicker(int track) {
	startButtons.get(track).setText("Start");
	tickers.stopPlaying(track);
    }


    public void tryToSetTempo(int track) {
	float newTempo=0; // never used, but compiler insists on initialisation...
	float oldTempo = tickers.getTempo(track);
	boolean tempoIsValid;
	try {
	    newTempo = Float.parseFloat(tempoFields.get(track).getText());
	    tempoIsValid = (newTempo > 0);
	} catch (NumberFormatException e) {
	    tempoIsValid = false;
	}
	if (tempoIsValid) {
	    tickers.setTempo(newTempo, track);
	} else {
	    tempoFields.get(track).setText(String.valueOf(oldTempo));
	}
    }


    public void addTrack() {
	if (numberOfTracks == maxTracksUsed) {
	    makeNewControlPanel("Track "+(numberOfTracks+1));
	    maxTracksUsed++;
	}
	// else: control panel has previously been made and removed: do nothing here

	controlPanelPanel.add(controlPanels.get(numberOfTracks));
	controlPanelPanelPane.validate();
	numberOfTracks++;

	// move scrollbar to end so that new controls are visible:
	JScrollBar theScrollBar = controlPanelPanelPane.getHorizontalScrollBar();
	theScrollBar.setValue(theScrollBar.getMaximum());

	controlPanelPanel.repaint(controlPanelPanel.getVisibleRect());

	tickers.addTrack();
    }


    public void removeTrack() {
	if (numberOfTracks==0) {
	    return;
	}
	numberOfTracks--;
	stopTicker(numberOfTracks); // maybe not necessary, but it doesn't hurt.
	tickers.removeTrack();
	controlPanelPanel.remove(controlPanels.get(numberOfTracks));
	controlPanelPanelPane.validate();
	controlPanelPanel.repaint(controlPanelPanel.getVisibleRect());
    }


    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(false);
	PMgui pmgui = new PMgui();
    }


    private static void loadBuiltInSounds() {
        java.net.URL soundList = PM.class.getResource("audio/sounds/contents.txt");
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
	    soundnames.add(soundName);
        }
    }


    public static void main(String[] args) throws InterruptedException {
	if (args.length>=1) {numberOfTracks=Integer.parseInt(args[0]);};
	if (args.length>=2) {bufferSize=Integer.parseInt(args[1]);}
// undocumented feature: can specify initial number of tracks
// and audio buffer size on the command line!

	sounds=new SoundCollection(sampleRate, sampleSize);
	loadBuiltInSounds();

	tickers=new Tickers(sounds, bufferSize);
	for (int i = 0; i < numberOfTracks; i++) {
	    tickers.addTrack();
	}
	tickers.start();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
