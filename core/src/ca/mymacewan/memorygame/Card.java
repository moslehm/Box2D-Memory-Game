package ca.mymacewan.memorygame;

class Card {

    private State state;
    private String value;
    private int cardID;
    private int index;

    Card() {
        state = State.HIDDEN;
    }

    String getValue() {
        return value;
    }

    State getState() {
        return state;
    }

    int getID() { return cardID; }

    void setState(State state) {
        this.state = state;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setID(int cardID) { this.cardID = cardID; }

    int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }
}