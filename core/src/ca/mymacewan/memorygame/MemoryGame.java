package Memory;

import java.util.ArrayList;
import java.util.Iterator;

import static Memory.State.HIDDEN;
import static Memory.State.PAIRED;
import static Memory.State.REVEALED;

public class MemoryGame {

    private ArrayList<Card> Cards;

    public MemoryGame() {

        // Implement dealing + shuffle in here.
    }

    public MemoryGame(int size) {

    }

    public void makeMove(int index) {

        if (!(isLegalMove(Cards.get(index)))) {
            return;
        }
        Iterator<Card> iter = Cards.iterator();
        while (iter.hasNext()) {
            Card i = iter.next();
            // FOR IF ARRAY HAS A CARD ALREADY REVEALED. HANDLES PAIRING AND MISMATCHES
            if (i.getState() == REVEALED) {
                Card j = Cards.get(index);
                if (j.getState() != REVEALED) {
                    j.setState(REVEALED);
                    if (i.getValue().equals(j.getValue())) {
                        i.setState(PAIRED);
                        j.setState(PAIRED);
                        return;
                    }
                    else {
                        i.setState(HIDDEN);
                        j.setState(HIDDEN);
                        return;
                    }
                }
            }
        }
        // FOR IF ARRAY HAS NO REVEALED CARDS
        Card i = Cards.get(index);
        if (i.getState() == HIDDEN) {
            i.setState(REVEALED);
            return;
        }
    }

    public boolean isGameOver() {
        Iterator<Card> iter = Cards.iterator();
        while (iter.hasNext()) {
            Card i = iter.next();
            if (i.getState() != PAIRED) {
                return false;
            }
        }
        return true;
    }

    public boolean isLegalMove(Card card) {
        if (Cards.contains(card)) {
            if (card.getState() == REVEALED || card.getState() == PAIRED) {
                return false;
            }
        }
        return true;
    }

    /* public void printCards() {
        iter = Cards.iterator();
        while (iter.hasNext()) {
            i = iter.next();
    } */

    private dealCards(int size) {
        
    }
}
