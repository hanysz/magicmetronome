package net.hanysz.MM.events;

public class AccelStartEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {

    private int accelMode = EXP_ACCEL_MODE;
    private MMEvent startTempo=null, endTempo=null;
    private String startTempoString="none", endTempoString="none";
    private int length = 0;

    public AccelStartEvent() {
	super(ACCEL_START_EVENT);
    }


    public void setMode(int mode) {
	accelMode = mode;
    }


    public int getMode() {
	return accelMode;
    }


    public void setStartTempo(MMEvent tempoEvent) {
	startTempo = tempoEvent;
	startTempoString = tempoEvent.toString();
    }


    public MMEvent getStartTempo() {
	return startTempo;
    }


    public void setEndTempo(MMEvent tempoEvent) {
	endTempo = tempoEvent;
	endTempoString = tempoEvent.toString();
    }


    public MMEvent getEndTempo() {
	return endTempo;
    }


    public void setLength(int length) {
	this.length = length;
    }


    public int getLength() {
	return length;
    }


    public String toString() {
	return super.toString()+"start of accel block, "
	    + ((accelMode==EXP_ACCEL_MODE) ? "exponential mode " : "linear mode ")
	    +"\n\tstart tempo: "+startTempoString
	    +"\n\tend tempo: "+endTempoString
	    +"\n\tlength : "+length;
    }
}
