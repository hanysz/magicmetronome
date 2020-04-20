package net.hanysz.MM.events;

public class RelativeTempoEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private float tempoRatio;

    public RelativeTempoEvent(float tempoRatio) {
	super(RELATIVE_TEMPO_EVENT);
	if (tempoRatio <= 0) {          // this should never happen
	    System.out.println(
		"Error in RelativeTempoEvent: trying to construct event "
		+ "with non-positive tempo "+tempoRatio);
	    System.exit(1);
	}
	this.tempoRatio = tempoRatio;
    }


    public float getTempoRatio() {
	return tempoRatio;
    }


    public String toString() {
	return super.toString()+"relative tempo ratio "+tempoRatio;
    }
}
