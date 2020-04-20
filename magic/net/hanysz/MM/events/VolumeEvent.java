package net.hanysz.MM.events;

public class VolumeEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private float volume;

    public VolumeEvent(float volume) {
	super(VOLUME_EVENT);
	this.volume = volume;
    }


    public float getVolume() {
	return volume;
    }


    public String toString() {
	return super.toString()+"volume: "+volume;
    }
}
