package net.hanysz.MM.events;

public class SoundEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private int whichSound;

    public SoundEvent(int whichSound) {
	super(SOUND_EVENT);
	this.whichSound = whichSound;
    }


    public int getSound() {
	return whichSound;
    }


    public String toString() {
	return super.toString()+"sound "+whichSound;
    }
}
