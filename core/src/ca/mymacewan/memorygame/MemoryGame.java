package ca.mymacewan.memorygame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import static ca.mymacewan.memorygame.State.HIDDEN;
import static ca.mymacewan.memorygame.State.PAIRED;
import static ca.mymacewan.memorygame.State.REVEALED;

public class MemoryGame {
    public int numOfCards;// How many cards in the game. it be increased when difficulty increase.
    private ArrayList<Card> Cards = new ArrayList<Card>(numOfCards);

    public void gameStart(){
        for (int i = 0 ; i < numOfCards; i++){
            Card tempCard = new Card();
            int cardValue = i/2;
            tempCard.setValue(Integer.toString(cardValue));
            //tempCard.setID(i);
            Cards.add(tempCard);
        }
        Collections.shuffle(Cards);
    }

 /*   public MemoryGame() {

        // Implement dealing + shuffle in here.

    }*/

    public void makeMove(int index) {
        if (!(isLegalMove(Cards.get(index)))) {
            return;
        }
        Iterator<Card> itr = Cards.iterator();
        while (itr.hasNext()) {
            Card i = itr.next();
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
        Iterator<Card> itr = Cards.iterator();
        while (itr.hasNext()) {
            Card i = itr.next();
            if (i.getState() != PAIRED) {
                return false;
            }
        }
        System.out.println("Congratulation!");
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

    public ArrayList<Card> getCards(){
        return Cards;
    }

    //format of output, [index]:[cardValue]:[cardState]
    public void printCards() {
        Iterator<Card> itr = Cards.iterator();
        int index = 0;
        while (itr.hasNext()) {
            Card theCard = itr.next();
            System.out.format("%-3d : %-3s : %-3S \n", index, theCard.getValue(), theCard.getState());
            index++;
        }
    }
}
