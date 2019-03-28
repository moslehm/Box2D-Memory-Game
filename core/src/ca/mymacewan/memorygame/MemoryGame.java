package ca.mymacewan.memorygame;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import static ca.mymacewan.memorygame.State.HIDDEN;
import static ca.mymacewan.memorygame.State.PAIRED;
import static ca.mymacewan.memorygame.State.REVEALED;


public class MemoryGame {
    private int numOfCards; // How many cards in the game. it be increased when difficulty increase.
    private ArrayList<Card> cards;
    private short [] difficulty = {8, 16, 24, 40, 52};
    private short diffLevel;
    private int score;
    private int combo;
    private long startTime;
    private long comboTime;
    private long idleStartTime;
    private static long isIdleTime = 90000L; //90 seconds;
    private static long comboInterval = 10000L;

    /**
     * Starts the game after numOfCards had been set
     */
    void gameStart(){
        numOfCards = difficulty[diffLevel];
        startTime = System.currentTimeMillis();
        combo = 2;
        cards = new ArrayList<Card>(numOfCards);
        for (int i = 0; i < numOfCards; i++) {
            Card tempCard = new Card();

            int cardValue = i / 2;
            tempCard.setValue(cardValue);
            tempCard.setKey(i);
            cards.add(tempCard);
        }
        Collections.shuffle(cards);
    }

    /**
     * Sets the card at index to REVEALED if isLegalMove returns true
     * @param index of card in cards
     */
    void flipUp(int index) {
        Card card = getCard(index);
        if (!(isLegalMove(card))) {
            return;
        }
        idleStartTime = System.currentTimeMillis();
        card.setState(REVEALED);
        for (Card currentCard : cards) {
            if (!(currentCard.equals(card)) && currentCard.getValue() == card.getValue()) {
                if (currentCard.getState() == REVEALED) {
                    currentCard.setState(PAIRED);
                    card.setState(PAIRED);
                    updateScore();
                    comboTime = System.currentTimeMillis() - startTime;
                    System.out.println("MATCH! comboTime:" + Long.toString(comboTime));
                    return;
                }
            }
        }
        // Commented out because the code above has the same functionality but for multiple cards
        /*Iterator<Card> itr = cards.iterator();
        while (itr.hasNext()) {
            Card i = itr.next();
            // FOR IF ARRAY HAS A CARD ALREADY REVEALED. HANDLES PAIRING AND MISMATCHES
            if (i.getState() == REVEALED) {
                Card j = cards.get(index);
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
        // FOR IF ARRAY HAS NO REVEALED cards
        Card i = cards.get(index);
        if (i.getState() == HIDDEN) {
            i.setState(REVEALED);
            return;
        }*/
    }


    /**
     * Sets the card at index to HIDDEN if not PAIRED
     * @param index of card in cards
     */
    void flipDown(int index){
        Card card = getCard(index);
        if (card.getState() != PAIRED){
            card.setState(HIDDEN);
        }
    }


    /**
     * @return false if any of the cards are not PAIRED, returns true otherwise
     */
    boolean isRoundOver() {
        for (Card i : cards) {
            if (i.getState() != PAIRED) {
                return false;
            }
        }
        System.out.println("Congratulation!");
        return true;
    }

    /**
     * @param card to check
     * @return false if card is not HIDDEN, returns true otherwise
     */
    private boolean isLegalMove(Card card) {
        if (cards.contains(card)) {
            return card.getState() == HIDDEN;
        }
        return true;
    }

    boolean isGameOver() {
        if (isRoundOver() && (diffLevel == 4)) {
            System.out.println("Congratulation!");
            return true;
        } else {
            return false;
        }
    }

    public int getDifficulty(){
        return diffLevel;
    }

    private Card getCard(int index) {
        for (Card currentCard : cards) {
            if (currentCard.getKey() == index) {
                return currentCard;
            }
        }
        return null;
    }

    /**
     * Format of output: [index]:[cardValue]:[cardState]
     * Used for testing, prints all the information of the cards in the game
     */
    public void printCards() {
        Iterator<Card> itr = cards.iterator();
        int index = 0;
        while (itr.hasNext()) {
            Card theCard = itr.next();
            System.out.format("%-3d : %-3s : %-3S \n", index, theCard.getValue(), theCard.getState());
            index++;
        }
    }

    /**
     * @return the cards
     */
    ArrayList<Card> getCards() {
        return cards;
    }

    public void updateScore() {
        if (comboTime <= comboInterval) {
            score += 100;
            combo = 2;
            System.out.println("NOT A COMBO");
        } else {
            score += 100 * combo;
            System.out.println("COMBO");
            if (combo < 4) {
                combo += 1;
            }
        }
    }

    public int getScore() {
        return score;
    }

    public void nextDiff() {
        if (diffLevel < 4) {
            diffLevel++;
        }
    }

    // FOR TESTING/DEMOING PURPOSES ONLY
    public void prevDiff(){
        if (diffLevel > 0){
            diffLevel--;
        }
    }

    //run this function for each touch
    public void resetIdleTime() {
        idleStartTime = System.currentTimeMillis();
    }

    //run this function for each frame to test is idle or not
    //restart the game for returning true
    public boolean isIdle() {
        if (System.currentTimeMillis() - idleStartTime > isIdleTime) {
            return true;
        } else {
            return false;
        }
    }
}
