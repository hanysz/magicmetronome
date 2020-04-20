package net.hanysz.MM.events;

public class RepeatStartEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private int repeatCount;

    public RepeatStartEvent(int repeatCount) {
	super(REPEAT_START_EVENT);
	this.repeatCount = repeatCount;
    }


    public int getRepeatCount() {
	return repeatCount;
    }

    public String toString() {
	String repeatCountString;

	if (repeatCount == INFINITE_REPEAT) {
	    repeatCountString = "ad infinitum";
	} else if (repeatCount == 1) {
	    repeatCountString = "once only";
	} else {
	    repeatCountString = repeatCount+" times";
	}
	return super.toString()+"repeat "+repeatCountString+".";
    }
}
