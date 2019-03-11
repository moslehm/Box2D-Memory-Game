package ca.mymacewan.memorygame;

class Card {

    private State state;
    private String value;
    private int key;

    Card() {
        state = State.HIDDEN;
    }

    String getValue() {
        return value;
    }

    State getState() {
        return state;
    }


    void setState(State state) {
        this.state = state;
    }

    void setValue(String value) {
        this.value = value;
    }


    int getKey() {
        return key;
    }

    void setKey(int key) {
        this.key = key;
    }
}