package net.hanysz.MM.events;

public class AccelEndEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {

    public AccelEndEvent() {
	super(ACCEL_END_EVENT);
    }

    public String toString() {
	return super.toString()+"end of accel block";
    }
}
