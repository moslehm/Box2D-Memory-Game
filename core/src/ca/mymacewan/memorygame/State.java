package Memory;

/* HIDDEN - Facedown card.
   REVEALED - Faceup card not paired. Max of one at a time.
   PAIRED - Card solved.
 */

public enum State {
    HIDDEN, REVEALED, PAIRED;
}
