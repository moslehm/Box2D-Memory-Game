package ca.mymacewan.memorygame;

import com.badlogic.gdx.physics.box2d.Body;

public class Box {
    private Body boxBody;
    private float scaleX;
    private Card card;
    private int id;
    private int alpha;

    Box(Body boxBody, float scaleX, Card card){
        this.boxBody = boxBody;
        this.scaleX = scaleX;
        this.card = card;
    }

    public Body getBody() {
        return boxBody;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX){
        this.scaleX = scaleX;
    }

    public Card getCard() {
        return card;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
