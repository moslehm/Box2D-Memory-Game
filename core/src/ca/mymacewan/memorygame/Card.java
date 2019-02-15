package ca.mymacewan.memorygame;

public class Card {

    private State state;
    private String value;
    private int cardID;

    public Card() {
        state = State.HIDDEN;
    }

    public String getValue() {
        return value;
    }

    public State getState() {
        return state;
    }

    public int getID() { return cardID; }

    public void setState(State state) {
        this.state = state;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setID(int cardID) { this.cardID = cardID; }
}