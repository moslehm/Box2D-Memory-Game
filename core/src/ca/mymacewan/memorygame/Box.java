package ca.mymacewan.memorygame;

import com.badlogic.gdx.physics.box2d.Body;

public class Box {
    private Body boxBody;
    private float scaleX;
    private float scaleY;
    private Card card;
    private float alpha;

    Box(Body boxBody, Card card){
        this.boxBody = boxBody;
        this.scaleX = 1f;
        this.scaleY = 1f;
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


    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }
}
