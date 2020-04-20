package net.hanysz.MM.events;

public class AbsoluteTempoEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private float tempo;

    public AbsoluteTempoEvent(float tempo) {
	super(ABSOLUTE_TEMPO_EVENT);
	if (tempo <= 0) {          // this should never happen
	    System.out.println(
		"Error in TempoEvent: trying to construct event "
		+ "with non-positive tempo "+tempo);
	    System.exit(1);
	}
	this.tempo = tempo;
    }


    public float getTempo() {
	return tempo;
    }


    public String toString() {
	return super.toString()+"absolute tempo "+tempo;
    }
}
