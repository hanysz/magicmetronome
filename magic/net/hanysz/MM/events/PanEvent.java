package net.hanysz.MM.events;

public class PanEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private int pan;

    public PanEvent(int pan) {
	super(VOLUME_EVENT);
	this.pan = pan;
    }


    public int getPan() {
	return pan;
    }


    public String toString() {
	return super.toString()+"pan: "+pan;
    }
}
