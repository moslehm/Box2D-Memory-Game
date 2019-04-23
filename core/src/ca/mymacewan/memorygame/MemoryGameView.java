package ca.mymacewan.memorygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import jwinpointer.JWinPointerReader;

public class MemoryGameView extends Game implements ApplicationListener {
    JWinPointerReader jWinPointerReader;

    @Override
    public void create() {
        jWinPointerReader = new JWinPointerReader("MemoryGameView");
        this.setScreen(new GameScreen(this, jWinPointerReader));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        Assets.dispose();
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