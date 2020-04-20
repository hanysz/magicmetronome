package net.hanysz.MM.events;

public class PushTempoEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {


    public PushTempoEvent() {
	super(PUSH_TEMPO_EVENT);
    }

    public String toString() {
	return super.toString()+"push tempo";
    }
}
