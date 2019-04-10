package ca.mymacewan.memorygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import jwinpointer.JWinPointerReader;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import java.util.ArrayList;

public class GameScreen implements Screen, InputProcessor, JWinPointerReader.PointerEventListener {
    // Box2D initialization
    // PHYSICS_ENTITY will always collide with WORLD_ENTITY
    protected OrthographicCamera camera;
    static float CAMERA_WIDTH_PIXELS;
    static float CAMERA_HEIGHT_PIXELS;
    float CAMERA_WIDTH_METERS;
    float CAMERA_HEIGHT_METERS;
    //final short PHYSICS_ENTITY = 0x1;    // 0001
    //final short WORLD_ENTITY = 0x1 << 1; // 0010 or 0x2 in hex
    protected Box2DDebugRenderer box2DDebugRenderer;
    protected World world;
    private ArrayList<Box> boxes = new ArrayList<Box>();
    protected Body groundBody;
    public ArrayList<GameScreen.TouchInfo> arrayOfTouchInfo = new ArrayList<GameScreen.TouchInfo>();
    protected Array<Joint> frictionJoints = new Array<Joint>();
    protected Body hitBodies[] = new Body[200];
    protected Body hitBody = null;

    SpriteBatch batch;
    Label[] scoreLabels;
    Label[] timerLabels;
    private TextureRegion backSideTexture;
    private Sprite[][] frontSideSprites;
    private MemoryGame game;
    ArrayList<Card> cards;
    int difficulty;
    private float halfBoxSizes[] = {1.5f, 1.5f, 1f, 0.8f, 0.7f, 0.7f, 0.6f, 0.5f, 0.5f};
    private float xyBoxSpacing[][] = {{3.1f, 1.9f}, {3.7f, 1.9f}, {5f, 2f}, {3.7f, 2.5f}, {3f, 2f}, {2.7f, 1.7f}, {2f, 1.6f}, {1.9f, 1.4f}, {2f, 1.1f}};
    private float timeLimits[] = {0f, 45f, 90f, 120f, 120f, 160f, 190f, 240f, 300f};
    int currentNumOfCards;
    private float currentTime;
    int currentScore;
    ShapeRenderer shapeRenderer;
    ArrayList<Box[]> boxPairs;
    ArrayList<Box[]> boxesInContact;
    ParticleEffect particleEffect;
    public Color worldColor;
    InputMultiplexer plex;
    Stage stage;

    //private Sound impactSound;
    private Sound pairSound;
    private Sound turnOverSound;
    private Sound winSound;

    private static TweenManager tweenManager;
    private JWinPointerReader jWinPointerReader;

    private Game parentGame;


    public GameScreen(Game parent, JWinPointerReader jWinPointerReader) {
        this.parentGame = parent;
        this.jWinPointerReader = jWinPointerReader;
    }

    @Override
    public void show() {
        stage = new Stage();
        shapeRenderer = new ShapeRenderer();
        boxPairs = new ArrayList<Box[]>();
        boxesInContact = new ArrayList<Box[]>();
        particleEffect = new ParticleEffect();
        particleEffect.load(Gdx.files.internal("explosion.p"), Gdx.files.internal(""));

        plex = new InputMultiplexer();
        float red = 63;
        float green = 168;
        float blue = 65;
        worldColor = new Color(red / 255f, green / 255f, blue / 255f, 1f);

        // "Meters" are the units of Box2D
        // 1 pixel = 0.018 meters
        // 1 meter = 55.556 pixels
        // This can be changed. The lower the meters are, the more the screen "zooms in".
        CAMERA_WIDTH_PIXELS = 1920;
        CAMERA_HEIGHT_PIXELS = 1080;
        CAMERA_WIDTH_METERS = toMeters(CAMERA_WIDTH_PIXELS);
        CAMERA_HEIGHT_METERS = toMeters(CAMERA_HEIGHT_PIXELS);

        // Tween setup
        Tween.setCombinedAttributesLimit(1);
        Tween.registerAccessor(Box.class, new BoxAccessor());
        tweenManager = new TweenManager();

        // setup the camera. In Box2D we operate on a
        // meter scale, pixels won't do it. So we use
        // an orthographic camera with a viewport of
        // CAMERA_WIDTH_METERS in width and CAMERA_HEIGHT_METERS meters in height.
        // We also position the camera so that it
        // looks at (0,0) (that's where the middle of the
        // screen will be located).
        camera = new OrthographicCamera(CAMERA_WIDTH_METERS, CAMERA_HEIGHT_METERS);
        camera.position.set(0, 0, 0);

        backSideTexture = new TextureRegion(new Texture(Gdx.files.internal("cardBack.png")));
        Texture unmatchedTextureSheet = new Texture(Gdx.files.internal("unmatched.png"));
        Texture matchedTextureSheet = new Texture(Gdx.files.internal("matched.png"));
        TextureRegion[][] unmatchedTextureRegions = TextureRegion.split(unmatchedTextureSheet, 276, 276);
        TextureRegion[][] matchedTextureRegions = TextureRegion.split(matchedTextureSheet, 276, 276);
        int columns = 10;
        int rows = 9;
        Sprite[] unmatchedFrontSideSprites = new Sprite[columns * rows];
        Sprite[] matchedFrontSideSprites = new Sprite[columns * rows];
        int index = 0;
        Sprite tempSprite;
        for (int y = 0; y < columns; y++) {
            for (int x = 0; x < rows; x++) {
                tempSprite = new Sprite(unmatchedTextureRegions[x][y]);
                tempSprite.setOrigin(toPixels(halfBoxSizes[difficulty]), toPixels(halfBoxSizes[difficulty]));
                unmatchedFrontSideSprites[index] = tempSprite;

                tempSprite = new Sprite(matchedTextureRegions[x][y]);
                tempSprite.setOrigin(toPixels(halfBoxSizes[difficulty]), toPixels(halfBoxSizes[difficulty]));
                matchedFrontSideSprites[index] = tempSprite;

                index++;
            }
        }
        frontSideSprites = new Sprite[][]{unmatchedFrontSideSprites, matchedFrontSideSprites};

        // Create the debug box2DDebugRenderer
        box2DDebugRenderer = new Box2DDebugRenderer();

        // Start the memory game
        game = new MemoryGame();
        game.gameStart();
        currentScore = game.getScore();

        // Create the world for the Box2D bodies
        world = new World(new Vector2(0, 0), true);

        // We also need an invisible zero size ground body
        // to which we can connect the mouse joints and friction joints
        BodyDef bodyDef = new BodyDef();
        groundBody = world.createBody(bodyDef);

        // Call abstract method to populate the world
        createWorld(world);

        // Creates the boxes and joints
        createGame();

        //load sound effects
        loadSound();

        // Batch to draw textures
        batch = new SpriteBatch();

        scoreLabels = addScoreActors(stage, currentScore);
        setTimerLabels();

        // Set the input processor as the ones overridden in here
        plex.addProcessor(this);
        plex.addProcessor(stage);
        Gdx.input.setInputProcessor(plex);

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body firstBody = contact.getFixtureA().getBody();
                Body secondBody = contact.getFixtureB().getBody();
                if (firstBody.getUserData() != null && secondBody.getUserData() != null) {
                    Box firstBox = (Box) firstBody.getUserData();
                    Box secondBox = (Box) secondBody.getUserData();
                    if (firstBox.getCard().getValue() == secondBox.getCard().getValue()) {
                        boxesInContact.add(new Box[]{firstBox, secondBox});
                        Vector2 contactPointInPixels = newCoords(contact.getWorldManifold().getPoints()[0]);
                        firstBox.setPointOfContact(contactPointInPixels);
                        secondBox.setPointOfContact(contactPointInPixels);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                Body firstBody = contact.getFixtureA().getBody();
                Body secondBody = contact.getFixtureB().getBody();
                if (firstBody.getUserData() != null && secondBody.getUserData() != null) {
                    Box firstBox = (Box) firstBody.getUserData();
                    Box secondBox = (Box) secondBody.getUserData();
                    for (Box currentBoxesInContact[] : boxesInContact) {
                        if (currentBoxesInContact[0] == firstBox || currentBoxesInContact[0] == secondBox) {
                            if (currentBoxesInContact[1] == firstBox || currentBoxesInContact[1] == secondBox) {
                                boxesInContact.remove(currentBoxesInContact);
                                currentBoxesInContact[0].resetPointOfContact();
                                currentBoxesInContact[1].resetPointOfContact();
                                break;
                            }
                        }
                    }
                }

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });

        jWinPointerReader.addPointerEventListener(this);
    }

    public static Label[] addScoreActors(Stage stage, int currentScore) {
        // Load font
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        BitmapFont font = new BitmapFont(Gdx.files.internal("KenPixelBlocks.fnt"));
        labelStyle.font = font;
        Label[] label = new Label[4];
        label[0] = new Label(Integer.toString(currentScore), labelStyle);
        label[1] = new Label(Integer.toString(currentScore), labelStyle);
        label[2] = new Label(Integer.toString(currentScore), labelStyle);
        label[3] = new Label(Integer.toString(currentScore), labelStyle);

        // Display score
        Vector2 bottomLeftCornerPos = new Vector2(0, 0);
        Vector2 bottomRightCornerPos = new Vector2(Gdx.graphics.getWidth(), 0);
        Vector2 topLeftCornerPos = new Vector2(0, Gdx.graphics.getHeight());
        Vector2 topRightCornerPos = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // define this
        float scoreDistanceFromCorner = Gdx.graphics.getHeight() / 8f - 10f;

        Vector2 bottomLeftScorePos = bottomLeftCornerPos.cpy().add(new Vector2(1, 1).cpy().scl(scoreDistanceFromCorner));
        Vector2 topLeftScorePos = topLeftCornerPos.cpy().add(new Vector2(1, -1).cpy().scl(scoreDistanceFromCorner));
        Vector2 bottomRightScorePos = bottomRightCornerPos.cpy().add(new Vector2(-1, 1).cpy().scl(scoreDistanceFromCorner + 15));
        Vector2 topRightScorePos = topRightCornerPos.cpy().add(new Vector2(-1, -1).cpy().scl(scoreDistanceFromCorner + 15));

        Container container = new Container(label[0]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(bottomLeftScorePos.x, bottomLeftScorePos.y);
        container.setRotation(315);
        stage.addActor(container);
        container = new Container(label[1]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(topLeftScorePos.x, topLeftScorePos.y);
        container.setRotation(225);
        stage.addActor(container);
        container = new Container(label[2]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(bottomRightScorePos.x, bottomRightScorePos.y);
        container.setRotation(135 - 90);
        stage.addActor(container);
        container = new Container(label[3]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(topRightScorePos.x, topRightScorePos.y);
        container.setRotation(45 + 90);
        stage.addActor(container);
        return label;
    }

    void setTimerLabels() {
        // Load font
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        BitmapFont font = new BitmapFont(Gdx.files.internal("KenPixelBlocks.fnt"));
        labelStyle.font = font;
        timerLabels = new Label[4];
        for (int i = 0; i < 4; i++) {
            timerLabels[i] = new Label("00:00", labelStyle);
        }

        Vector2 bottomLeftCornerPos = new Vector2(0, 0);
        Vector2 bottomRightCornerPos = new Vector2(Gdx.graphics.getWidth(), 0);
        Vector2 topLeftCornerPos = new Vector2(0, Gdx.graphics.getHeight());
        Vector2 topRightCornerPos = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        float scoreDistanceFromCorner = Gdx.graphics.getHeight() / 10f - 10f;

        Vector2 bottomLeftScorePos = bottomLeftCornerPos.cpy().add(new Vector2(1, 1).cpy().scl(scoreDistanceFromCorner));
        Vector2 topLeftScorePos = topLeftCornerPos.cpy().add(new Vector2(1, -1).cpy().scl(scoreDistanceFromCorner));
        Vector2 bottomRightScorePos = bottomRightCornerPos.cpy().add(new Vector2(-1, 1).cpy().scl(scoreDistanceFromCorner + 15));
        Vector2 topRightScorePos = topRightCornerPos.cpy().add(new Vector2(-1, -1).cpy().scl(scoreDistanceFromCorner + 15));

        Container container = new Container(timerLabels[0]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(bottomLeftScorePos.x, bottomLeftScorePos.y);
        container.setRotation(315);
        stage.addActor(container);
        container = new Container(timerLabels[1]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(topLeftScorePos.x, topLeftScorePos.y);
        container.setRotation(225);
        stage.addActor(container);
        container = new Container(timerLabels[2]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(bottomRightScorePos.x, bottomRightScorePos.y);
        container.setRotation(135 - 90);
        stage.addActor(container);
        container = new Container(timerLabels[3]);
        container.setTransform(true);
        container.setScale(1 - (CAMERA_WIDTH_PIXELS - Gdx.graphics.getWidth())/Gdx.graphics.getWidth());
        container.setPosition(topRightScorePos.x, topRightScorePos.y);
        container.setRotation(45 + 90);
        stage.addActor(container);
    }

    @Override
    public void render(float delta) {
        update();

        particleEffect.update(Gdx.graphics.getDeltaTime());
        currentTime += Gdx.graphics.getRawDeltaTime();

        if (difficulty != 0 && currentTime > timeLimits[difficulty]) {
            parentGame.setScreen(new ScoreboardScreen(parentGame, jWinPointerReader, worldColor, currentScore));
        }

        // Background colour
        Gdx.gl.glClearColor(worldColor.r, worldColor.g, worldColor.b, worldColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update the world with a fixed time step
        world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

        // Clear the screen and setup the projection matrix
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        tweenManager.update(Gdx.graphics.getDeltaTime());

        // Render each box via the SpriteBatch.
        // Set the projection matrix of the SpriteBatch to the camera's combined matrix.
        // This will make the SpriteBatch work in world coordinates (meters)
        batch.getProjectionMatrix().set(camera.combined);
        batch.begin();
        // Draw backside first to make the front side of the cards on top
        for (int i = 0; i < boxes.size(); i++) {
            Box box = boxes.get(i);
            Body boxBody = box.getBody();
            Vector2 position = boxBody.getPosition(); // Get the box's center position
            float angle = MathUtils.radiansToDegrees * boxBody.getAngle(); // Get the box's rotation angle around the center

            // x, y: Unrotated position of bottom left corner of the box
            // originX, originY: Rotation center relative to the bottom left corner of the box
            // width, height: width and height of the box
            // scaleX, scaleY: Scale on the x- and y-axis
            // Draw the front side
            float halfSize = halfBoxSizes[difficulty];
            batch.draw(backSideTexture,
                    position.x - halfSize, position.y - halfSize,
                    halfSize, halfSize,
                    halfSize * 2, halfSize * 2,
                    (float) Math.max(-Math.cos(box.getScaleX() * Math.PI), 0), 1f,
                    angle);
        }
        batch.end();

        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        for (Box[] pair : boxPairs) {
            drawLine(pair[0].getBody().getPosition(), pair[1].getBody().getPosition());
        }

        batch.begin();
        // Draw front
        for (int i = 0; i < boxes.size(); i++) {
            Box box = boxes.get(i);
            Body boxBody = box.getBody();
            Vector2 position = boxBody.getPosition(); // Get the box's center position
            float angle = MathUtils.radiansToDegrees * boxBody.getAngle(); // Get the box's rotation angle around the center
            Sprite currentSprite = frontSideSprites[cards.get(i).getState().getValue()][cards.get(i).getValue()];
            currentSprite.setAlpha(box.getAlpha());
            Vector2 spritePosition = newCoords(position);
            currentSprite.setX(spritePosition.x - currentSprite.getWidth() / 2f);
            currentSprite.setY(spritePosition.y - currentSprite.getHeight() / 2f);
            currentSprite.setScale((float) Math.abs(Math.min(-Math.cos(box.getScaleX() * Math.PI), 0)), box.getScaleY());
            currentSprite.setRotation(angle);
            currentSprite.draw(batch);
        }
        batch.end();

        batch.begin();
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        particleEffect.draw(batch);
        batch.end();


        batch.getProjectionMatrix().set(camera.combined);
        stage.act();
        stage.draw();
        // Render the world using the debug box2DDebugRenderer to view bodies and joints
        //box2DDebugRenderer.render(world, camera.combined);
    }

    Vector2 newCoords(Vector2 oldCoords) {
        Vector2 newCoords = new Vector2();
        float switchToResolutionX = toPixels(oldCoords.x) / CAMERA_WIDTH_PIXELS * Gdx.graphics.getWidth();
        float switchToResolutionY = toPixels(oldCoords.y) / CAMERA_HEIGHT_PIXELS * Gdx.graphics.getHeight();
        newCoords.x = Gdx.graphics.getWidth() / 2f + switchToResolutionX;
        newCoords.y = Gdx.graphics.getHeight() / 2f + switchToResolutionY;
        return newCoords;
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

    public void drawLine(Vector2 p1, Vector2 p2) {
        p1 = newCoords(p1);
        p2 = newCoords(p2);

        float scaleBy = toPixels(0.5f);
        Vector2 lineMidPoint = (p2.cpy().sub(p1)).cpy().scl(0.5f).cpy().add(p1);
        Vector2 lineUnitVector = (p2.cpy().sub(p1)).cpy().nor();
        Vector2 midSegmentStartPoint = lineMidPoint.cpy().sub(lineUnitVector.cpy().scl(scaleBy));
        Vector2 midSegmentEndPoint = lineMidPoint.cpy().add(lineUnitVector.cpy().scl(scaleBy));

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.195f, 0.64f, 0.94f, 1);
        shapeRenderer.rectLine(p1, p2, 2f);
        shapeRenderer.end();


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1);

        shapeRenderer.rectLine(midSegmentStartPoint, midSegmentEndPoint, 4f);

        shapeRenderer.end();

        DrawArrow(midSegmentStartPoint, midSegmentEndPoint);
        DrawArrow(midSegmentEndPoint, midSegmentStartPoint);
    }

    public void DrawArrow(Vector2 origin, Vector2 endpoint) {
        // Draw arrowhead so we can see direction
        Vector2 arrowDirection = origin.cpy().sub(endpoint);
        DebugDrawArrowhead(endpoint, arrowDirection.cpy().nor(), toPixels(0.2f));
    }


    private void DebugDrawArrowhead(Vector2 origin, Vector2 direction, float size) {
        float theta = 30.0f;
        // Theta angle is the acute angle of the arrow, so flip direction or else arrow will be pointing "backwards"
        Vector2 arrowheadHandle = direction.cpy().scl(-1f).cpy().scl(size);

        Quaternion arrowRotationR = new Quaternion(new Vector3(0, 0, 1), theta);
        Quaternion arrowRotationL = new Quaternion(new Vector3(0, 0, 1), -theta);
        Vector2 arrowheadR = arrowheadHandle.cpy().rotate(arrowRotationR.getAngle());
        Vector2 arrowheadL = arrowheadHandle.cpy().rotate(arrowRotationL.getAngle());
        Vector2 rightSide = origin.cpy().add(arrowheadR);
        Vector2 leftSide = origin.cpy().add(arrowheadL);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rectLine(origin, rightSide, 4f);
        shapeRenderer.rectLine(origin, leftSide, 4f);
        shapeRenderer.end();
    }

    protected void createWorld(World world) {
        float boardWidth = CAMERA_WIDTH_METERS;
        float boardHeight = CAMERA_HEIGHT_METERS;
        float halfWidth = boardWidth / 2f;
        float halfHeight = boardHeight / 2f;
        float littleSpaceAtTheEdge = 0.4f;
        float cornerDist = halfHeight / 2f;
        // Ground body
        {
            BodyDef bd = new BodyDef();
            bd.position.set(0, 0);
            groundBody = world.createBody(bd);

            EdgeShape shape = new EdgeShape();

            FixtureDef sd = new FixtureDef();
            sd.shape = shape;
            sd.density = 0;
            sd.restitution = 0.4f;

            // Draws sides around the board
            // LEFT
            shape.set(new Vector2(-halfWidth + littleSpaceAtTheEdge, -halfHeight), new Vector2(-halfWidth + littleSpaceAtTheEdge, halfHeight));
            groundBody.createFixture(sd);

            // RIGHT
            shape.set(new Vector2(halfWidth - littleSpaceAtTheEdge, -halfHeight), new Vector2(halfWidth - littleSpaceAtTheEdge, halfHeight));
            groundBody.createFixture(sd);

            // TOP
            shape.set(new Vector2(-halfWidth, halfHeight - littleSpaceAtTheEdge), new Vector2(halfWidth, halfHeight - littleSpaceAtTheEdge));
            groundBody.createFixture(sd);

            // BOTTOM
            shape.set(new Vector2(-halfWidth, -halfHeight + littleSpaceAtTheEdge), new Vector2(halfWidth, -halfHeight + littleSpaceAtTheEdge));
            groundBody.createFixture(sd);

            // Draws the angled sides placed in the corners of the screen
            // BOTTOM LEFT
            shape.set(new Vector2(-halfWidth + littleSpaceAtTheEdge, -cornerDist), new Vector2(-halfWidth + cornerDist, -halfHeight + littleSpaceAtTheEdge));
            groundBody.createFixture(sd);

            // BOTTOM RIGHT
            shape.set(new Vector2(halfWidth - littleSpaceAtTheEdge - cornerDist, -halfHeight + littleSpaceAtTheEdge), new Vector2(halfWidth - littleSpaceAtTheEdge, -halfHeight + littleSpaceAtTheEdge + cornerDist));
            groundBody.createFixture(sd);

            // TOP LEFT
            shape.set(new Vector2(-halfWidth + littleSpaceAtTheEdge, halfHeight - cornerDist), new Vector2(-halfWidth + cornerDist, halfHeight - littleSpaceAtTheEdge));
            groundBody.createFixture(sd);

            // TOP RIGHT
            shape.set(new Vector2(halfWidth - littleSpaceAtTheEdge - cornerDist, halfHeight - littleSpaceAtTheEdge), new Vector2(halfWidth - littleSpaceAtTheEdge, halfHeight - littleSpaceAtTheEdge - cornerDist));
            groundBody.createFixture(sd);

            shape.dispose();
        }
    }

    private void checkForMatches() {
        for (Box[] boxPairInContact : boxesInContact) {
            Card firstCard = boxPairInContact[0].getCard();
            Card secondCard = boxPairInContact[1].getCard();
            if (firstCard.getState() == State.REVEALED && secondCard.getState() == State.REVEALED) {
                if (firstCard.getValue() == secondCard.getValue()) {
                    game.setToPaired(firstCard, secondCard);
                    for (Box[] pair : boxPairs) {
                        if (pair[0] == boxPairInContact[0] || pair[0] == boxPairInContact[1]) {
                            if (pair[1] == boxPairInContact[0] || pair[1] == boxPairInContact[1]) {
                                boxPairs.remove(pair);
                                break;
                            }
                        }
                    }
                    pairSound.play(1.0f);
                    Vector2 contactPoint = (boxPairInContact[0].getPointOfContact());
                    particleEffect.getEmitters().first().setPosition(contactPoint.x, contactPoint.y);
                    particleEffect.start();
                }
            }
        }
    }
    private void animateFlippingCard(Box box, int targetValue) {
        // Kill current tween - or pre-existing
        tweenManager.killTarget(box);

        // Scale X down
        Tween.to(box, BoxAccessor.SCALE_X, 0.3f)
                .target(targetValue)
                .ease(TweenEquations.easeInOutSine)
                .start(tweenManager);

    }

    private void createGame() {
        boxPairs = new ArrayList<Box[]>();
        boxesInContact = new ArrayList<Box[]>();
        hitBodies = new Body[200];
        frictionJoints = new Array<Joint>();
        cards = game.getCards();
        difficulty = game.getDifficulty();
        currentNumOfCards = 0;

        for (Sprite[] frontSideSpriteArray : frontSideSprites) {
            for (Sprite frontSideSprite : frontSideSpriteArray) {
                frontSideSprite.setSize(((halfBoxSizes[difficulty] * 2f) / CAMERA_WIDTH_METERS) * Gdx.graphics.getWidth(), ((halfBoxSizes[difficulty] * 2f) / CAMERA_HEIGHT_METERS) * Gdx.graphics.getHeight());
                frontSideSprite.setOrigin(((halfBoxSizes[difficulty]) / CAMERA_WIDTH_METERS) * Gdx.graphics.getWidth(), ((halfBoxSizes[difficulty]) / CAMERA_HEIGHT_METERS) * Gdx.graphics.getHeight());
            }
        }

        float halfWidth = CAMERA_WIDTH_METERS / 2f;
        float halfHeight = CAMERA_HEIGHT_METERS / 2f;

        float goldenAngle = (float) ((2 * Math.PI) / Math.pow((1f + Math.sqrt(5)) / 2f, 2));
        float radius;
        float maxRadius = 3.5f;
        float scalingFactor = 0f;
        float angle;
        int k = 0;
        float xPosition;
        float yPosition;
        boolean xPointInRange;
        boolean yPointInRange;
        float littleSpaceAtTheEdge = 0.4f;

        while (currentNumOfCards < cards.size()) {
            // Box bodies
            angle = k * goldenAngle;
            radius = scalingFactor * (float) Math.sqrt(k);
            scalingFactor = (float) (maxRadius / Math.sqrt(k)) - 1f;
            xPosition = (float) (radius * Math.cos(angle) * xyBoxSpacing[difficulty][0] - 0.2f);
            yPosition = (float) (radius * Math.sin(angle) * xyBoxSpacing[difficulty][1]);
            xPointInRange = xPosition < halfWidth - littleSpaceAtTheEdge && xPosition > -halfWidth + littleSpaceAtTheEdge;// && xPosition > 0;
            yPointInRange = yPosition < halfHeight - littleSpaceAtTheEdge && yPosition > -halfHeight + littleSpaceAtTheEdge;// && yPosition > 0;
            if (xPointInRange && yPointInRange && k > 3) {
                createBox(xPosition, yPosition, angle, cards.get(currentNumOfCards));
                currentNumOfCards++;
            }
            k++;
        }
    }

    boolean roundInProgress = true;

    void update() {
        currentScore = game.getScore();
        scoreLabels[0].setText(currentScore);
        scoreLabels[1].setText(currentScore);
        scoreLabels[2].setText(currentScore);
        scoreLabels[3].setText(currentScore);

        updateTimeLabels();
        checkForMatches();
        if (game.isIdle()) {
            destroyAll();
            boxPairs = new ArrayList<Box[]>();
            game.gameStart();
            createGame();
        }
        if (game.isRoundOver() && roundInProgress) {
            if (game.isGameOver()) {
                parentGame.setScreen(new ScoreboardScreen(parentGame, jWinPointerReader, worldColor, currentScore));
            }
            roundInProgress = false;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    nextLevel();
                }
            }, 2);
        }
        updateCards();
    }

    private void updateTimeLabels() {
        if (difficulty != 0) {
            float timeLeft = timeLimits[difficulty] - currentTime;
            if ((timeLeft / 60f) < 1) {
                for (Label timer : timerLabels) {
                    timer.setText("00:" + (int) timeLeft);
                }
            } else {
                float minutes = (int) (timeLeft / 60f);
                float seconds = ((timeLeft / 60f) - minutes) * 60f;
                for (Label timer : timerLabels) {
                    timer.setText((int) minutes + ":" + (int) seconds);
                }
            }
        }
    }

    void nextLevel() {
        // Round over
        // Destroy bodies and joints
        // Then start next round
        winSound.play(1.0f);
        currentTime = 0;
        destroyAll();
        boxPairs = new ArrayList<Box[]>();
        game.nextDiff();
        game.gameStart();
        createGame();
        roundInProgress = true;
    }

    void destroyAll() {
        if (!world.isLocked()) {
            // Destroy joints one by one using this
            for (Joint jointToDestroy : frictionJoints) {
                if (jointToDestroy != null) {
                    world.destroyJoint(jointToDestroy);
                    jointToDestroy = null;
                }
            }

            for (GameScreen.TouchInfo touchPoint : arrayOfTouchInfo) {
                if (touchPoint.mouseJoint != null) {
                    world.destroyJoint(touchPoint.mouseJoint);
                    touchPoint.mouseJoint = null;
                }
            }
            Body bod;
            while (boxes.size() > 0) {
                Box box = boxes.get(0);
                bod = box.getBody();
                if (bod != null) {
                    world.destroyBody(bod);
                    bod = null;
                }
                boxes.remove(0);

            }
            boxes.removeAll(boxes);
        }
    }

    void loadSound() {
        pairSound = Gdx.audio.newSound(Gdx.files.internal("Sound Effects/pair.mp3"));
        turnOverSound = Gdx.audio.newSound(Gdx.files.internal("Sound Effects/turnOver.mp3"));
        winSound = Gdx.audio.newSound(Gdx.files.internal("Sound Effects/Winning&nextLevel.mp3"));
    }

    public void createBox(float xPosition, float yPosition, float angle, Card card) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfBoxSizes[difficulty], halfBoxSizes[difficulty]);

        PolygonShape body = new PolygonShape();
        body.setAsBox(halfBoxSizes[difficulty], halfBoxSizes[difficulty]);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 1.0f;
        fd.friction = 0.3f;

        // Create the BodyDef, set a position, and other properties.
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = BodyDef.BodyType.DynamicBody;
        boxBodyDef.position.x = xPosition;
        boxBodyDef.position.y = yPosition;
        //boxBodyDef.angle = angle;
        Body boxBody = world.createBody(boxBodyDef);
        boxBody.createFixture(fd);

        Box box = new Box(boxBody, card);
        boxBody.setUserData(box);

        // Add the box to our list of boxes
        boxes.add(box);

        // Motor joint body
        // This is just stationary a point at center of each box
        // Motor joints will be connected to this to snap the boxes back
        // to their original positions
        Body jointBody;
        BodyDef jointBodyDef = new BodyDef();
        jointBodyDef.position.set(xPosition, yPosition);
        jointBody = world.createBody(jointBodyDef);

        // Friction joint
        // Connected between each box and the ground body
        // Used to make the movement of boxes more "realistic"
        FrictionJointDef jd = new FrictionJointDef();
        jd.localAnchorA.set(0, 0);
        jd.localAnchorB.set(0, 0);
        jd.bodyA = groundBody;
        jd.bodyB = boxBody;
        jd.collideConnected = true;
        jd.maxForce = boxBody.getMass();//mass * gravity;
        jd.maxTorque = 20;//mass * radius * gravity;
        frictionJoints.add(world.createJoint(jd));
        shape.dispose();
    }

    @Override
    public void dispose() {
        box2DDebugRenderer.dispose();
        world.dispose();
        backSideTexture.getTexture().dispose();
        batch.dispose();
        shapeRenderer.dispose();
        particleEffect.dispose();

        //dispose sound effects
        pairSound.dispose();
        turnOverSound.dispose();
        winSound.dispose();

        box2DDebugRenderer = null;
        world = null;
        //mouseJoints = null;
        hitBodies = null;
    }


    public Vector2 vectorToMeters(Vector2 vectorInPixels) {
        return new Vector2(CAMERA_WIDTH_PIXELS / 2f + toMeters(vectorInPixels.x), CAMERA_HEIGHT_PIXELS / 2f + toMeters(vectorInPixels.y)); // DEFAULT: 0.018f
    }

    public Vector2 vectorToPixels(Vector2 vectorInMeters) {
        return new Vector2(Gdx.graphics.getWidth() / 2f + toPixels(vectorInMeters.x), Gdx.graphics.getHeight() / 2f + toPixels(vectorInMeters.y)); // DEFAULT: 0.018f
    }

    public float toMeters(float pixels) {
        return pixels * 0.013f;
    }

    public float toPixels(float meters) {
        return meters / 0.013f;
    }

    // Instantiate vector and the callback here to avoid errors from the GC
    Vector3 testPoint = new Vector3();
    QueryCallback callback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            // Checks if the hit point is inside the fixture of the body
            if (fixture.testPoint(testPoint.x, testPoint.y)) {
                hitBody = fixture.getBody();
                return false;
            } else
                return true;
        }
    };


    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return false;
    }

    class TouchInfo {
        public int pointer;
        public MouseJoint mouseJoint;

        TouchInfo(int pointer, MouseJoint mouseJoint) {
            this.pointer = pointer;
            this.mouseJoint = mouseJoint;
        }

    }

    private boolean realTouchDown(int x, int y, int pointer) {
        game.resetIdleTime();

        // Converts the mouse coordinates to meters (world coordinates)
        camera.unproject(testPoint.set(x, y, 0));
        // Checks which bodies are within the given bounding box around the mouse pointer
        hitBodies[pointer] = null;
        hitBody = hitBodies[pointer];

        world.QueryAABB(callback, testPoint.x - 0.0001f, testPoint.y - 0.0001f, testPoint.x + 0.0001f, testPoint.y + 0.0001f);

        if (hitBody == groundBody) hitBody = null;

        // Ignores kinematic bodies
        if (hitBody != null && hitBody.getType() == BodyDef.BodyType.KinematicBody) return false;

        // If hitBodies points to a valid body.
        // We change start the flip animation then we create
        // a new mouse joint and attach it to the hit body.
        if (hitBody != null) {
            for (Box box : boxes) {
                if (box.getBody() == hitBody) {
                    Card boxCard = box.getCard();
                    if (boxCard.getState() == State.HIDDEN) {
                        animateFlippingCard(box, 0);
                        //System.out.println("Flipping card at index: " + boxCard.getKey());
                        Card[] newPair = game.flipUp(boxCard.getKey());
                        turnOverSound.play(1f);
                        if (newPair != null) {
                            int numOfBoxes = 0;
                            boxPairs.add(new Box[2]);
                            for (Box currentBox : boxes) {
                                if (currentBox.getCard() == newPair[0] || currentBox.getCard() == newPair[1]) {
                                    boxPairs.get(boxPairs.size() - 1)[numOfBoxes] = currentBox;
                                    numOfBoxes++;
                                }
                            }
                        }
                    }
                }
            }
            // Create mouse joint because hitBodies is a box, even if it's not flipping
            MouseJointDef def = new MouseJointDef();
            def.bodyA = groundBody;
            def.bodyB = hitBody;
            def.collideConnected = true;
            def.target.set(testPoint.x, testPoint.y);
            def.maxForce = 1000000.0f * hitBody.getMass();

            GameScreen.TouchInfo newTouchInfo = new GameScreen.TouchInfo(pointer, (MouseJoint) world.createJoint(def));
            arrayOfTouchInfo.add(newTouchInfo);
            hitBody.setAwake(true);
        }
        return false;
    }

    //another temporary vector
    Vector2 target = new Vector2();

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return false;
    }

    private boolean realTouchDragged(int x, int y, int pointer) {
        game.resetIdleTime();

        // if a mouse joint exists we simply update
        // the target of the joint based on the new
        // mouse coordinates
        MouseJoint targetMouseJoint = null;
        for (GameScreen.TouchInfo touchPoint : arrayOfTouchInfo) {
            if (touchPoint.pointer == pointer) {
                targetMouseJoint = touchPoint.mouseJoint;
            }
        }
        if (targetMouseJoint != null) {
            camera.unproject(testPoint.set(x, y, 0));
            targetMouseJoint.setTarget(target.set(testPoint.x, testPoint.y));
        }
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return false;
    }

    private boolean realTouchUp(int x, int y, int pointer) {
        game.resetIdleTime();

        for (GameScreen.TouchInfo touchPoint : arrayOfTouchInfo) {
            if (touchPoint.pointer == pointer && touchPoint.mouseJoint != null) {
                world.destroyJoint(touchPoint.mouseJoint);
                touchPoint.mouseJoint = null;
            }
        }
        return false;
    }

    void updateCards() {
        boolean notMoving;
        boolean notTweening;
        boolean noMouseJoint;
        for (Box box : boxes) {
            Card boxCard = box.getCard();
            notMoving = !box.getBody().isAwake();
            notTweening = box.getScaleX() == 0;
            noMouseJoint = box.getBody().getJointList().size <= 1;

            if (notMoving && notTweening && noMouseJoint) {
                if (boxCard.getState() != State.PAIRED) {
                    animateFlippingCard(box, 1);
                    for (Box[] pair : boxPairs) {
                        if (pair[0].getCard() == boxCard || pair[1].getCard() == boxCard) {
                            boxPairs.remove(pair);
                            break;
                        }
                    }
                    game.flipDown(boxCard.getKey());
                    turnOverSound.play(1f);
                }
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.RIGHT) {
            nextLevel();
        } else if (keycode == Input.Keys.LEFT) {
            prevLevel();
        }
        return false;
    }

    // FOR TESTING/DEMOING PURPOSES ONLY
    private void prevLevel() {
        // Round over
        // Destroy bodies and joints
        // Then start next round
        currentTime = 0;
        destroyAll();
        boxPairs = new ArrayList<Box[]>();
        game.prevDiff();
        game.gameStart();
        createGame();
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }


    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    // Initialize constant variables to help differentiate between mouse events
    private static final int EVENT_TYPE_DRAG = 1;
    private static final int EVENT_TYPE_HOVER = 2;
    private static final int EVENT_TYPE_DOWN = 3;
    private static final int EVENT_TYPE_UP = 4;
    private static final int EVENT_TYPE_BUTTON_DOWN = 5;
    private static final int EVENT_TYPE_BUTTON_UP = 6;
    private static final int EVENT_TYPE_IN_RANGE = 7;
    private static final int EVENT_TYPE_OUT_OF_RANGE = 8;

    @Override
    public void pointerXYEvent(int deviceType, final int pointerID, int eventType, boolean inverted, final int x, final int y, int pressure) {
        /*System.out.println("deviceType: " + deviceType);
        System.out.println("pointerID: " + pointerID);
        System.out.println("eventType: " + eventType);
        System.out.println("inverted: " + inverted);
        System.out.println("x, y: " + x + ", " + y);
        System.out.println("pressure: " + pressure);
        System.out.println();*/

        if (deviceType == 2 || deviceType == 0) {
            switch (eventType) {
                case EVENT_TYPE_DOWN:
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // process the result, e.g. add it to an Array<Result> field of the ApplicationListener.
                            realTouchDown(x, y, pointerID);
                        }
                    });
                    break;
                case EVENT_TYPE_DRAG:
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // process the result, e.g. add it to an Array<Result> field of the ApplicationListener.
                            realTouchDragged(x, y, pointerID);
                        }
                    });
                    break;
                case EVENT_TYPE_UP:
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // process the result, e.g. add it to an Array<Result> field of the ApplicationListener.
                            realTouchUp(x, y, pointerID);
                        }
                    });
                    break;
            }
        }
    }

    @Override
    public void pointerButtonEvent(int i, int i1, int i2, boolean b, int i3) {

    }

    @Override
    public void pointerEvent(int i, int i1, int i2, boolean b) {

    }
}
