package ca.mymacewan.memorygame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import jwinpointer.JWinPointerReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class ScoreboardScreen implements Screen {
    private final int playerScore;
    private Game parent;
    private Color worldColor;
    private Stage stage;
    private JWinPointerReader jWinPointerReader;
    private boolean screenIsTouched;
    private ArrayList<Integer> highScores;
    private boolean isAlreadyTouched;


    public ScoreboardScreen(Game parent, JWinPointerReader jWinPointerReader, Color worldColor, int playerScore) {
        this.parent = parent;
        this.jWinPointerReader = jWinPointerReader;
        this.worldColor = worldColor;
        this.playerScore = playerScore;
    }

    @Override
    public void show() {
        stage = new Stage();
        screenIsTouched = false;
        highScores = new ArrayList<Integer>();
        isAlreadyTouched = Gdx.input.isTouched();

        GameScreen.addScoreActors(stage, playerScore);

        try {
            getScores();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        highScores.add(playerScore);
        Collections.sort(highScores);
        Collections.reverse(highScores);
        if (highScores.size() > 10){
            highScores.remove(10);
        }

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        BitmapFont font = new BitmapFont(Gdx.files.internal("KenPixelBlocks.fnt"));
        labelStyle.font = font;
        Label label = new Label("High scores", labelStyle);
        label.setAlignment(Align.center);
        label.setPosition(Gdx.graphics.getWidth()/2f - label.getWidth()/2f, Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/7f);
        stage.addActor(label);

        boolean playerScoreDisplayed = false;
        for (int i = 0; i < highScores.size() - 1; i++){
            // Display scores
            int currentScore =  highScores.get(i);
            label = new Label((i + 1) + ". " + currentScore, labelStyle);
            label.setAlignment(Align.center);
            label.setPosition(Gdx.graphics.getWidth()/2f - label.getWidth()/2f, (Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/7f) - Gdx.graphics.getHeight()/15f * (i + 1));
            if (!playerScoreDisplayed && currentScore == playerScore){
                label.setColor(Color.RED);
                playerScoreDisplayed = true;
            }
            stage.addActor(label);
        }

        try {
            saveScores();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Texture texture = new Texture(Gdx.files.internal("returnButton.png"));
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

        Gdx.input.setInputProcessor(stage);*/
    }

    private void getScores() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("highscores.txt");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        highScores = (ArrayList<Integer>) objectInputStream.readObject();
        objectInputStream.close();
    }

    private void saveScores() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("highscores.txt");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(highScores);
        objectOutputStream.close();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(worldColor.r, worldColor.g, worldColor.b, worldColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        if (!isAlreadyTouched) {
            if (Gdx.input.isTouched()) {
                screenIsTouched = true;
            }
        }
        else if (!Gdx.input.isTouched()){
            isAlreadyTouched = false;
        }
        if (!Gdx.input.isTouched() && screenIsTouched){
            parent.setScreen(new GameScreen(parent, jWinPointerReader));
        }
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
