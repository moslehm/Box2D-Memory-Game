package Memory;

public class Card {

    private State state;
    private String value;

    public Card() {
        state = State.HIDDEN;
    }

    public String getValue() {
        return value;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setValue(String value) {
        this.value = value;
    }
}