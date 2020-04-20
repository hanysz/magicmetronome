package net.hanysz.MM.events;

public class RepeatEndEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {

    private int repeatsRemaining = 0;


    public RepeatEndEvent() {
	super(REPEAT_END_EVENT);
    }


    public void setRepeatsRemaining(int number) {
	repeatsRemaining = number;
    }


    public int getRepeatsRemaining() {
	return repeatsRemaining;
    }


    public String toString() {
	return super.toString()+"end of repeat block";
    }
}
