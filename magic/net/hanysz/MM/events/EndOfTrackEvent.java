package net.hanysz.MM.events;

public class EndOfTrackEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {


    public EndOfTrackEvent() {
	super(END_OF_TRACK_EVENT);
    }

    public String toString() {
	return super.toString()+"end of track";
    }
}
