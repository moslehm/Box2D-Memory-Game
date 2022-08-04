package ca.mymacewan.memorygame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import jwinpointer.JWinPointerReader;

public class MemoryGameView extends Game implements ApplicationListener {
    JWinPointerReader jWinPointerReader;

    @Override
    public void create() {
        // jWinPointerReader is used for multitouch on Windows touch screens, currently not working
        // jWinPointerReader = new JWinPointerReader("Memory Game");
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