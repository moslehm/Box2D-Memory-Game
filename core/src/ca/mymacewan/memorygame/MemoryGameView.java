package ca.mymacewan.memorygame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;

import static java.lang.Math.sqrt;

public class MemoryGameView extends ApplicationAdapter {
	SpriteBatch batch;
    MemoryGame game;
	Stage stage;
    int gameSize = 16;

	public class CardGroup extends Group {
		Texture backTexture, frontTexture;
		Image displayedImage;

		final Card card;

		public CardGroup(final Card card, int x, int y){
			backTexture = Assets.manager.get(Assets.cardBack);
			frontTexture = Assets.manager.get(Assets.demoCard + card.getValue() + ".png");
			displayedImage = new Image(backTexture);

			float[] convertedXY = convertXY(x, y, (int) (gameSize/sqrt(gameSize)));

			displayedImage.setPosition(convertedXY[0], convertedXY[1]);
			displayedImage.setOrigin(Align.center);
			this.addActor(displayedImage);

			setOrigin(Align.center);

			this.card = card;
			setUserObject(this.card);

			addListener((new ClickListener(){
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					if (game.isLegalMove((Card)getUserObject())) {
						RunnableAction changeTexture = new RunnableAction() {
							@Override
							public void run() {
								displayedImage.setDrawable(new SpriteDrawable(new Sprite(frontTexture)));
							}
						};
						displayedImage.addAction(Actions.sequence(
								MyActions.flipOut(0.1f),
								changeTexture,
								MyActions.flipIn(0.1f)));

						((Card)getUserObject()).setID(pointer);
						((Card)getUserObject()).setState(State.REVEALED);
					}
					return true;
				}
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                    if (((Card)getUserObject()).getState()!= State.PAIRED && ((Card)getUserObject()).getID() == pointer){
						RunnableAction changeTexture = new RunnableAction() {
							@Override
							public void run() {
								displayedImage.setDrawable(new SpriteDrawable(new Sprite(backTexture)));
							}
						};
						displayedImage.addAction(Actions.sequence(
								MyActions.flipOut(0.1f),
								changeTexture,
								MyActions.flipIn(0.1f)));
						//if ()
						((Card) getUserObject()).setState(State.HIDDEN);
					}
				}
			}));
		}

		private float[] convertXY(int x, int y, int cardsPerRow){
			float xOffset = Gdx.graphics.getWidth() - (cardsPerRow * 150f + (float)(10 * (cardsPerRow - 1)));
			float yOffset = Gdx.graphics.getHeight() - (cardsPerRow * 150f + (float)(10  * (cardsPerRow - 1)));
			float[] convertedXY = {0f, 0f};

			convertedXY[0] = xOffset/2f + ((160f * ((float) x)));
			convertedXY[1] = yOffset/2f + ((160f * ((float) y)));

			return convertedXY;
		}

		@Override
		public void draw(Batch batch, float alpha) {
				displayedImage.draw(batch, alpha);
		}
	}

	@Override
	public void create () {
    	Assets.load();
    	Assets.manager.finishLoading();

		batch = new SpriteBatch();

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

        game = new MemoryGame();
        game.numOfCards = gameSize;
        game.gameStart();

        int gameRows = (int)(gameSize/sqrt(gameSize));
        ArrayList<Card> cards = game.getCards();
		for (int x = 0; x < gameRows; x++){
			for (int y = 0; y < gameRows; y++){
				Card currentCard = cards.get((x * gameRows) + y);
				CardGroup newCardGroup = new CardGroup(currentCard, x, y);

				stage.addActor(newCardGroup);
			}
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(44/255f, 135/255f, 209/255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		Assets.dispose();
		stage.dispose();
	}
}