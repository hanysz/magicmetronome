package net.hanysz.MM.events;

/* MMEvent class: items to be placed in an MMEventList
 *
 * All the interesting stuff is in the subclasses:
 * AbsoluteTempoEvent
 * AccelEndEvent
 * AccelStartEvent
 * EndOfTrackEvent
 * MarkerEvent
 * MMEvent
 * PanEvent
 * PauseEvent
 * PopTempoEvent
 * PushTempoEvent
 * RelativeTempoEvent
 * RepeatEndEvent
 * RepeatStartEvent
 * SoundEvent
 * VolumeEvent
 */

public class MMEvent implements net.hanysz.MM.MMConstants {
/*  -- don't need to store slot number, it will be stored in the MMEventList ?
    private int slot;    // there is one slot per tick,
                         // except where repeats complicate things!
*/
    private int eventType;


    public MMEvent(int type) {
	this.eventType = type;
    }


    public int getType() {
	return eventType;
    }


    public String toString() {
	return "MMEvent ";  // toString of the subclass will do the rest
    }


/*
    public MMEvent(int slot, int type) {
	this.slot = slot;
	this.eventType = type;
    }
*/

/*
    public String toString() {
	return slot+": "; // toString of the subclass will do the rest
    }
*/

}



