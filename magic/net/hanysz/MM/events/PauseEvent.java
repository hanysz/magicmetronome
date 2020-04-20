package net.hanysz.MM.events;

public class PauseEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private int numberOfTicks;

    public PauseEvent(int numberOfTicks) {
	super(VOLUME_EVENT);
	this.numberOfTicks = numberOfTicks;
    }

    public String toString() {
	return super.toString()+"pause: "+numberOfTicks;
    }
}
