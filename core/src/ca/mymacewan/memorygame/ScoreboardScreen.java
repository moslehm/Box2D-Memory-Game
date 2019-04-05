package ca.mymacewan.memorygame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import jwinpointer.JWinPointerReader;

public class ScoreboardScreen implements Screen {
    private Game parent;
    private Color worldColor;
    private Stage stage;
    private JWinPointerReader jWinPointerReader;


    public ScoreboardScreen(Game parent, JWinPointerReader jWinPointerReader, Color worldColor) {
        this.parent = parent;
        this.jWinPointerReader = jWinPointerReader;
        this.worldColor = worldColor;
    }

    @Override
    public void show() {
        stage = new Stage();
        Texture texture = new Texture(Gdx.files.internal("returnButton.png"));
        Image returnButton = new Image(texture);

        Container wrapper = new Container(returnButton);
        wrapper.setTransform(true);
        wrapper.setSize(returnButton.getWidth() - 20, returnButton.getHeight() - 20);
        wrapper.setPosition(10, Gdx.graphics.getHeight() - wrapper.getHeight() - 10f);
        wrapper.setTouchable(Touchable.enabled);

        //first option
        wrapper.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent.setScreen(new GameScreen(parent, jWinPointerReader));
            }
        });
        stage.addActor(wrapper);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(worldColor.r, worldColor.g, worldColor.b, worldColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
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
