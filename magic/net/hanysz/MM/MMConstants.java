package net.hanysz.MM;

public interface MMConstants {
    public static final int START_OF_BUFFER = 0;
    public static final int START_OF_BYTES = 0;

    public static final int MARKER_NOT_FOUND = -1;

    public static final int DEFAULT_SAMPLE_RATE = 44100;
    public static final int DEFAULT_SAMPLE_SIZE = 16;

    public static final float DEFAULT_TEMPO = 60.0f;
    public static final String DEFAULT_TEMPO_STRING = "60";

    public static final float DEFAULT_VOLUME = 100;

    public static final int PAN_CENTRE = 0;
    public static final int PAN_LEFT = -127;
    public static final int PAN_RIGHT = 127;

    // types of event in event list:
    public static final int SOUND_EVENT = 1;
    public static final int ABSOLUTE_TEMPO_EVENT = 2;
    public static final int RELATIVE_TEMPO_EVENT = 3;
    public static final int REPEAT_START_EVENT = 4;
    public static final int REPEAT_END_EVENT = 5;
    public static final int ACCEL_START_EVENT = 6;
    public static final int ACCEL_END_EVENT = 7;
    public static final int END_OF_TRACK_EVENT = 8;
    public static final int MARKER_EVENT = 9;
    public static final int PUSH_TEMPO_EVENT = 10;
    public static final int POP_TEMPO_EVENT = 11;
    public static final int RECALL_TEMPO_EVENT = 12;
    public static final int VOLUME_EVENT = 13;
    public static final int PAN_EVENT = 14;
    public static final int BRANCH_EVENT = 15;

    public static final int INFINITE_REPEAT = 0;


    public static final int NO_ACCEL_MODE = 0;
    public static final int EXP_ACCEL_MODE = 1;
    public static final int LINEAR_ACCEL_MODE = 2;

    /** Pauses are implemented by inserting sound number
    SILENT_SOUND into the event list.
    */
    public static final int SILENT_SOUND = -1;
}
