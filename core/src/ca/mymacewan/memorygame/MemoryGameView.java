package ca.mymacewan.memorygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import jwinpointer.JWinPointerReader;

public class MemoryGameView extends Game implements ApplicationListener {
    JWinPointerReader jWinPointerReader;

    private Sound impactSound;
    private Sound pairSound;
    private Sound turnOverSound;
    private Sound winSound;

    @Override
    public void create() {
        jWinPointerReader = new JWinPointerReader("MemoryGameView");
        this.setScreen(new GameScreen(this, jWinPointerReader));
        //this.setScreen(new ScoreboardScreen(this, jWinPointerReader, Color.BLACK, 400));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        //dispose sound effects
        impactSound.dispose();
        pairSound.dispose();
        turnOverSound.dispose();
        winSound.dispose();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}