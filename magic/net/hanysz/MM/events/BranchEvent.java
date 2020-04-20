package net.hanysz.MM.events;

public class BranchEvent extends MMEvent
			implements net.hanysz.MM.MMConstants {
    private MMEventList branch;


    public BranchEvent(MMEventList branch) {
	super(BRANCH_EVENT);
	this.branch = branch;
    }


    /** nb this returns a clone of the branch,
     * to avoid confusion if a branch happens within a repeat
     * (i.e, the same MMEventList is used multiple times).
     */
    public MMEventList getBranch() {
	return (MMEventList)branch.clone();
    }

    public String toString() {
	return super.toString()+"branch:\n\n"+ branch.toString()
		+"---end of branch---\n";
    }
}
