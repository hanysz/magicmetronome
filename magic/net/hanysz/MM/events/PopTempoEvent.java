package net.hanysz.MM.events;

public class PopTempoEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {


    public PopTempoEvent() {
	super(POP_TEMPO_EVENT);
    }

    public String toString() {
	return super.toString()+"pop tempo";
    }
}
