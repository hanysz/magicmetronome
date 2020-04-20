package net.hanysz.MM.events;

/* MMEventList class: stores a list of MMEvents
 * first version, 26th January 2007
 */

import java.util.*;

public class MMEventList implements net.hanysz.MM.MMConstants, Cloneable {

    private ArrayList<MMEvent> eventList = new ArrayList<MMEvent>();
    private Stack<Integer> repeatStack = new Stack<Integer>();
    private int position = 0;
    private int length = 0;
    private Set<String> markerNames = new HashSet<String>();

    /* generally the default no-arg constructor will be used,
    	but a constructor used to be provided so that new branches will
	inherit tempo, volume and pan attributes */


/*
    public MMEventList() {
	// do nothing--all fields are initialised above
    }
*/


    public void add(MMEvent event) {
	eventList.add(event);
	if (event.getType() == MARKER_EVENT) {
	    markerNames.add(((MarkerEvent)event).getName());
	}
	length++;
    }


    public void resetPosition() {
	this.position = 0;
    }


    public MMEvent getNextEvent() {
	MMEvent nextEvent;

	if (position>=length) {
	    return(new EndOfTrackEvent());
	}
	nextEvent = eventList.get(position);
	position++;
	switch(nextEvent.getType()) {
	    case REPEAT_START_EVENT:
		repeatStack.push(position);
		repeatStack.push(((RepeatStartEvent)nextEvent).getRepeatCount());
		nextEvent = getNextEvent();
		break;
	    case REPEAT_END_EVENT:
		int repeatCount = repeatStack.pop();
		if (repeatCount==1) {
		    repeatStack.pop(); // remove position of repeat start
		}
		else {
		    if (repeatCount!=INFINITE_REPEAT) {
			repeatCount--;
		    }
		    position = repeatStack.peek();
		    repeatStack.push(repeatCount);
		}
		    ((RepeatEndEvent)nextEvent).setRepeatsRemaining(repeatCount);
		break;
	    default:
		break;
	}
	return nextEvent;
    }


    public boolean markerExists(String markerName) {
	return markerNames.contains(markerName);
    }


    public String toString() {
	StringBuilder theString = new StringBuilder();
	int i = 0;

	for (MMEvent theEvent : eventList) {
	    theString.append(i++ + ": "
	    		+ theEvent.toString() + "\n");
	}
	return theString.toString();
    }


    public Object clone() {
        try  {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new Error ("MMEventList clone() failed");
        }
    } // clone


    private void error (String message) {
        System.out.println("Error in class MMEventList: "
                            + message);
        System.exit(1);
    }
}

