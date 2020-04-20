package net.hanysz.MM.events;

public class MMEventTest {

    public static void main(String args[]) {
	MMEvent tester;
	MMEventList list, branch;

	list = new MMEventList();
	branch = new MMEventList();

	tester = new SoundEvent(3);
	System.out.println(tester);
	list.add(tester);

	tester = new AbsoluteTempoEvent(144);
	System.out.println(tester);
	list.add(tester);

	tester = new RelativeTempoEvent(1.2f);
	System.out.println(tester);
	list.add(tester);

	tester = new RepeatStartEvent(7);
	System.out.println(tester);
	list.add(tester);

	tester = new RepeatStartEvent(0);
	System.out.println(tester);
	list.add(tester);

	tester = new RepeatStartEvent(1);
	System.out.println(tester);
	list.add(tester);

	branch.add(new EndOfTrackEvent());
	branch.add(new RepeatStartEvent(34));
	branch.add(new AbsoluteTempoEvent(20));
	branch.add(new PopTempoEvent());
	tester = new BranchEvent(branch);
	System.out.println(tester);
	list.add(tester);

	tester = new RepeatEndEvent();
	System.out.println(tester);
	list.add(tester);

	tester = new MarkerEvent("some_silly_marker_name");
	System.out.println(tester);
	list.add(tester);

	tester = new PushTempoEvent();
	System.out.println(tester);
	list.add(tester);

	tester = new PopTempoEvent();
	System.out.println(tester);
	list.add(tester);

	tester = new EndOfTrackEvent();
	System.out.println(tester);
	list.add(tester);

	tester = new AccelStartEvent();
	System.out.println(tester);
	list.add(tester);

	tester = new VolumeEvent(1.2803f);
	System.out.println(tester);
	list.add(tester);

	tester = new PanEvent(-15);
	System.out.println(tester);
	list.add(tester);

	tester = new AccelEndEvent();
	System.out.println(tester);
	list.add(tester);

	System.out.println();
	System.out.println(list);
    }
}
