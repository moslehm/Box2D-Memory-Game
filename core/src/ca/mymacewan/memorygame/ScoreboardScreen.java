package ca.mymacewan.memorygame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

public class ScoreboardScreen implements Screen {
    private Game parent;
    private Color worldColor;
    public ScoreboardScreen(Game parent, Color worldColor) {
        this.parent = parent;
        this.worldColor = worldColor;
    }

    @Override
    public void show() {
        //parent.setScreen();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(worldColor.r, worldColor.g, worldColor.b, worldColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
