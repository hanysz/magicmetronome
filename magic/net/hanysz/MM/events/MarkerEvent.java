package net.hanysz.MM.events;

public class MarkerEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private String markerName;


    public MarkerEvent(String markerName) {
	super(MARKER_EVENT);
	this.markerName = new String(markerName);
    }


    public String getName() {
	return markerName;
    }

    public String toString() {
	return super.toString()+"marker \""+markerName+"\"";
    }
}
