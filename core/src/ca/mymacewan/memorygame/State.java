package ca.mymacewan.memorygame;

/* HIDDEN - Facedown card.
   REVEALED - Faceup card not paired. Max of one at a time.
   PAIRED - Card solved.
 */

public enum State {
    HIDDEN(0), REVEALED(0), PAIRED(1);
    private final int value;
    private State(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
