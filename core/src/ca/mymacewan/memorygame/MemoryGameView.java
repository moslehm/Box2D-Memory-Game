package ca.mymacewan.memorygame;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import jwinpointer.JWinPointerReader;
import jwinpointer.JWinPointerReader.PointerEventListener;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.physics.box2d.joints.MotorJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;


import java.util.ArrayList;

public class MemoryGameView implements ApplicationListener, InputProcessor, PointerEventListener {
    // Box2D initialization
    // PHYSICS_ENTITY will always collide with WORLD_ENTITY
    protected OrthographicCamera camera;
    //final short PHYSICS_ENTITY = 0x1;    // 0001
    //final short WORLD_ENTITY = 0x1 << 1; // 0010 or 0x2 in hex
    protected Box2DDebugRenderer renderer;
    protected World world;
    private ArrayList<Box> boxes = new ArrayList<Box>();
    protected Body groundBody;
    protected Array<MouseJoint> mouseJoints = new Array<MouseJoint>();
    protected Array<Joint> frictionJoints = new Array<Joint>();
    protected Array<Joint> motorJoints = new Array<Joint>();
    protected Body hitBodies[] = new Body[80];
    protected Body hitBody = null;

    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout textLayout = new GlyphLayout();
    private TextureRegion backSideTexture;
    private TextureRegion[] frontSideTextures;
    private MemoryGame game;
    ArrayList<Card> cards;
    int difficulty;
    private float halfBoxSizes[] = {0.8f, 0.7f, 0.6f, 0.5f, 0.3f};
    private float xyBoxSpacing[][] = {{2.3f, 1.3f}, {2.2f, 1.1f}, {1.8f, 0.9f}, {1.3f, 1f}, {1.9f, 1f}};
    int currentScore;

    private static TweenManager tweenManager;

    // == Test Start ==
    private static JWinPointerReader jWinPointerReader;
    // == Test End ==
    @Override
    public void create() {

        // "Meters" are the units of Box2D
        // 1 pixel = 0.018 meters
        // 1 meter = 55.556 pixels
        // This can be changed. The lower the meters are, the more the screen "zooms in".
        float CAMERA_WIDTH_METERS = toMeters(Gdx.graphics.getWidth());
        float CAMERA_HEIGHT_METERS = toMeters(Gdx.graphics.getHeight());

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

        // Create the debug renderer
        renderer = new Box2DDebugRenderer();

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

        // Batch to draw textures
        batch = new SpriteBatch();

        // Load font
        font = new BitmapFont(Gdx.files.internal("ArialFont.fnt"));


        // Set the input processor as the ones overridden in here
        //Gdx.input.setInputProcessor(this);

        backSideTexture = new TextureRegion(new Texture(Gdx.files.internal("cardBack.png")));
        Texture textureSheet = new Texture(Gdx.files.internal("AlphabetSheet.png"));
        TextureRegion[][] tmpRegions = TextureRegion.split(textureSheet, 150, 150);
        frontSideTextures = new TextureRegion[7 * 4];
        int index = 0;
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 4; x++) {
                frontSideTextures[index++] = tmpRegions[x][y];
            }
        }


        // == Windows multi touch test Start ==
        jWinPointerReader = new JWinPointerReader("MemoryGameView");
        jWinPointerReader.addPointerEventListener(this);
        // == Windows multi touch test End ==
    }


    @Override
    public void render() {
        update();

        // Background colour
        Gdx.gl.glClearColor(44 / 255f, 135 / 255f, 209 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update the world with a fixed time step
        world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

        // Clear the screen and setup the projection matrix
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        // Render the world using the debug renderer to view bodies and joints
        renderer.render(world, camera.combined);

        tweenManager.update(Gdx.graphics.getDeltaTime());


        // Render each box via the SpriteBatch.
        // Set the projection matrix of the SpriteBatch to the camera's combined matrix.
        // This will make the SpriteBatch work in world coordinates (meters)
        batch.getProjectionMatrix().set(camera.combined);
        batch.begin();

        // Draw backside first to make the front side of the cards on top
        for (int i = 0; i < boxes.size(); i++){
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

        // Draw front
        for (int i = 0; i < boxes.size(); i++) {
            Box box = boxes.get(i);
            Body boxBody = box.getBody();
            Vector2 position = boxBody.getPosition(); // Get the box's center position
            float angle = MathUtils.radiansToDegrees * boxBody.getAngle(); // Get the box's rotation angle around the center

            // Draw the back side
            // To make it set textures from the game logic, do frontSideTextures[card.getIndex] or something
            //System.out.println("i: " + Integer.toString(i));
            //System.out.println("cards.get(i).getValue()): " + cards.get(i).getValue());
            batch.draw(frontSideTextures[cards.get(i).getValue()], position.x - halfBoxSizes[difficulty], position.y - halfBoxSizes[difficulty],
                    halfBoxSizes[difficulty], halfBoxSizes[difficulty],
                    halfBoxSizes[difficulty] * 2, halfBoxSizes[difficulty] * 2,
                    (float) Math.abs(Math.min(-Math.cos(box.getScaleX() * Math.PI), 0)), 1f,
                    angle);
        }
        batch.end();

        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        // Display score
        font.draw(batch, Integer.toString(currentScore), Gdx.graphics.getWidth() / 2f - textLayout.width / 2f, Gdx.graphics.getHeight() / 2f + textLayout.height / 2f);
        batch.end();
    }

    protected void createWorld(World world) {
        float boardWidth = toMeters(Gdx.graphics.getWidth());
        float boardHeight = toMeters(Gdx.graphics.getHeight());
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

            // categoryBits is what the body is (a world entity)
            // maskBits is what it collides with (a physics entity)
            //sd.filter.categoryBits = WORLD_ENTITY;
            //sd.filter.maskBits = PHYSICS_ENTITY;



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

    private void tweenHelpingHand(Box box, int targetX) {
        // Kill current tween - or pre-existing
        tweenManager.killTarget(box);

        // Scale X down
        Tween.to(box, BoxAccessor.SCALE_X, 0.3f)
                .target(targetX)
                .ease(TweenEquations.easeInOutSine)
                .start(tweenManager);

    }

    int currentNumOfCards;

    private void createGame() {
            hitBodies = new Body[80];
            mouseJoints = new Array<MouseJoint>();
            frictionJoints = new Array<Joint>();
            motorJoints = new Array<Joint>();
        for (int i = 0; i < hitBodies.length; i++) {
            hitBodies[i] = null;
            mouseJoints.add(null);
            frictionJoints.add(null);
            motorJoints.add(null);
        }
        cards = game.getCards();
        difficulty = game.getDifficulty();
        currentNumOfCards = 0;

        float halfWidth = toMeters(Gdx.graphics.getWidth()) / 2f;
        float halfHeight = toMeters(Gdx.graphics.getHeight()) / 2f;

        float goldenAngle = (float) ((2 * Math.PI) / Math.pow((1f + Math.sqrt(5)) / 2f, 2));
        float radius = 0f;
        float maxRadius = 3.5f;
        float scalingFactor = 0f;
        float angle;
        int k = 0;
        float xPosition;
        float yPosition;
        boolean farFromAxis;
        boolean xPointInRange;
        boolean yPointInRange;
        boolean inRange;

        while (currentNumOfCards < cards.size()) {
            // Box bodies
            angle = k * goldenAngle;// * 0.367f;
            radius = scalingFactor * (float) Math.sqrt(k);
            scalingFactor = (float) (maxRadius / Math.sqrt(k)) - 1f;
            xPosition = (float) (radius * Math.cos(angle) * xyBoxSpacing[difficulty][0] - 0.2f);
            yPosition = (float) (radius * Math.sin(angle) * xyBoxSpacing[difficulty][1]);
            farFromAxis = xPosition > halfBoxSizes[difficulty] && yPosition > halfBoxSizes[difficulty];
            xPointInRange = xPosition < halfWidth && xPosition > -halfWidth;// && xPosition > 0;
            yPointInRange = yPosition < halfHeight && yPosition > -halfHeight;// && yPosition > 0;
            inRange = (30 < k && 33 > k) | 34 < k;
            if (xPointInRange && yPointInRange) {
                createBox(xPosition, yPosition, angle, cards.get(currentNumOfCards));
                currentNumOfCards++;
                //createBox(-xPosition, -yPosition, angle, cards.get(currentNumOfCards));
                //currentNumOfCards++;
                //createBox(xPosition, -yPosition, angle, cards.get(currentNumOfCards));
                //currentNumOfCards++;
                //createBox(-xPosition, yPosition, angle, cards.get(currentNumOfCards));
                //currentNumOfCards++;
            }
            k++;
        }
    }

    public void clearLevel() {
        Body bod;
        com.badlogic.gdx.utils.Array<JointEdge> Jlist;
        for (Box box : boxes) {
            bod = box.getBody();
            Jlist = bod.getJointList();
            for (JointEdge j : Jlist) {
                world.destroyJoint(j.joint);
            }
            world.destroyBody(bod);

        }
    }

    boolean roundInProgress = true;
    void update() {
        currentScore = game.getScore();
        textLayout.setText(font, Integer.toString(currentScore));
        if(game.isIdle()){
            //restart game here;
        }
        if (game.isRoundOver() && roundInProgress) {
            roundInProgress = false;
            nextLevel();
        }
        updateCards();
    }

    void nextLevel(){
        // Round over
        // Destroy bodies and joints
        // Then start next round
        destroyAll();
        game.nextDiff();
        game.gameStart();
        createGame();
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

            MouseJoint mouseJointToDestroy;
            for (int i = 0; i < mouseJoints.size; i++) {
                mouseJointToDestroy = mouseJoints.get(i);
                if (mouseJoints.get(i) != null) {
                    world.destroyJoint(mouseJointToDestroy);
                    mouseJoints.set(i, null);
                    mouseJointToDestroy = null;
                }
            }
            // One more for loop to remove the last kind of joint, motor joints
            // Use the motorJoints array
            Joint motorJointToDestroy;
            for (int i = 0; i < motorJoints.size; i++) {
                motorJointToDestroy = motorJoints.get(i);
                if (motorJoints.get(i) != null) {
                    world.destroyJoint(motorJointToDestroy);
                    motorJoints.set(i, null);

                }
            }
            Body bod;
            int size = boxes.size();
            while(boxes.size() > 0) {
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


    public void createBox(float xPosition, float yPosition, float angle, Card card) {

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfBoxSizes[difficulty], halfBoxSizes[difficulty]);

        PolygonShape body = new PolygonShape();
        body.setAsBox(halfBoxSizes[difficulty], halfBoxSizes[difficulty]);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 1.0f;
        fd.friction = 0.3f;
        //fd.filter.categoryBits = PHYSICS_ENTITY;
        //fd.filter.maskBits = WORLD_ENTITY;

        // Create the BodyDef, set a position, and other properties.
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = BodyType.DynamicBody;
        boxBodyDef.position.x = xPosition;
        boxBodyDef.position.y = yPosition;
        //boxBodyDef.angle = angle;
        Body boxBody = world.createBody(boxBodyDef);
        boxBody.createFixture(fd);

        Box box = new Box(boxBody, 1f, card);

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

        // Motor joint
        /*MotorJointDef jointDef = new MotorJointDef();
        jointDef.angularOffset = 0f;
        jointDef.collideConnected = false;
        jointDef.correctionFactor = 1f;
        jointDef.maxForce = 70f;
        jointDef.maxTorque = 50f;
        jointDef.initialize(jointBody, boxBody);
        motorJoints.set(currentNumOfCards, world.createJoint(jointDef));*/

        // Friction joint
        // Connected between each box and the ground body
        // Used to make the movement of boxes more "realistic"
        FrictionJointDef jd = new FrictionJointDef();
        jd.localAnchorA.set(0, 0);
        jd.localAnchorB.set(0, 0);
        jd.bodyA = groundBody;
        jd.bodyB = boxBody;
        jd.collideConnected = true;
        jd.maxForce = 20;//mass * gravity;
        jd.maxTorque = 20;//mass * radius * gravity;
        frictionJoints.add(world.createJoint(jd));
        shape.dispose();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        world.dispose();
        backSideTexture.getTexture().dispose();

        renderer = null;
        world = null;
        mouseJoints = null;
        hitBodies = null;
    }

    public float toMeters(float pixels) {
        return pixels * 0.013f; // DEFAULT: 0.018f
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
        System.out.println("touchDown: " + x + ", " + y);

        return realTouchDown(x, y, pointer);
    }

    private boolean realTouchDown(int x, int y, int pointer) {
        roundInProgress = true;
        game.resetIdleTime();

        // Converts the mouse coordinates to meters (world coordinates)
        camera.unproject(testPoint.set(x, y, 0));
        // Checks which bodies are within the given bounding box around the mouse pointer
        hitBodies[pointer] = null;
        hitBody = hitBodies[pointer];

        world.QueryAABB(callback, testPoint.x - 0.0001f, testPoint.y - 0.0001f, testPoint.x + 0.0001f, testPoint.y + 0.0001f);

        if (hitBody == groundBody) hitBody = null;

        // Ignores kinematic bodies
        if (hitBody != null && hitBody.getType() == BodyType.KinematicBody) return false;

        // If hitBodies points to a valid body.
        // We change start the flip animation then we create
        // a new mouse joint and attach it to the hit body.
        if (hitBody != null) {
            for (Box box : boxes) {
                if (box.getBody() == hitBody) {
                    Card boxCard = box.getCard();
                    if (boxCard.getState() == State.HIDDEN) {
                        tweenHelpingHand(box, 0);
                        //System.out.println("Flipping card at index: " + boxCard.getKey());
                        game.flipUp(boxCard.getKey());
                        box.setID(pointer);
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

            mouseJoints.insert(pointer, (MouseJoint) world.createJoint(def));
            hitBody.setAwake(true);
        }
        return false;
    }
    //another temporary vector
    Vector2 target = new Vector2();

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return realTouchDragged(x, y, pointer);
    }

    private boolean realTouchDragged(int x, int y, int pointer) {
        game.resetIdleTime();

        // if a mouse joint exists we simply update
        // the target of the joint based on the new
        // mouse coordinates
        MouseJoint targetMouseJoint = mouseJoints.get(pointer);
        if (targetMouseJoint != null) {
            camera.unproject(testPoint.set(x, y, 0));
            targetMouseJoint.setTarget(target.set(testPoint.x, testPoint.y));
        }
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return realTouchUp(x, y, pointer);
    }

    private boolean realTouchUp(int x, int y, int pointer) {
        game.resetIdleTime();
        //System.out.println("Pointer up: " + pointer);
        //System.out.println("mouseJoint null: " + (mouseJoint[pointer] != null));

        // if a mouse joint exists we simply destroy it
        if (mouseJoints.get(pointer) != null) {
            MouseJoint targetMouseJoint = mouseJoints.get(pointer);
            /*for (Box box : boxes) {
                if (box.getBody() == targetMouseJoint.getBodyB()) {
                    Card boxCard = box.getCard();
                    if (boxCard.getState() != State.PAIRED && !box.getBody().isAwake()) {
                        tweenHelpingHand(box, 1);
                        game.flipDown(boxCard.getKey());
                    }
                }
            }*/
            world.destroyJoint(targetMouseJoint);
            mouseJoints.set(pointer, null);
            targetMouseJoint = null;
        }
        return false;
    }

    void updateCards(){
        boolean notMoving;
        boolean notTweening;
        boolean noMouseJoint;
        for (Box box : boxes) {
            Card boxCard = box.getCard();
            notMoving =  !box.getBody().isAwake();
            notTweening = !tweenManager.containsTarget(box);
            noMouseJoint = box.getBody().getJointList().size <= 1;
            if (notMoving && notTweening && noMouseJoint) {
                if (boxCard.getState() != State.PAIRED) {
                    tweenHelpingHand(box, 1);
                    game.flipDown(boxCard.getKey());
                }
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.RIGHT){
            nextLevel();
        }
        else if(keycode == Input.Keys.LEFT){
            prevLevel();
        }
        return false;
    }

    // FOR TESTING/DEMOING PURPOSES ONLY
    private void prevLevel() {
        // Round over
        // Destroy bodies and joints
        // Then start next round
        destroyAll();
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

    public void pause() {

    }

    public void resume() {

    }

    public void resize(int width, int height) {

    }

    private static final int EVENT_TYPE_DRAG = 1;
    private static final int EVENT_TYPE_HOVER = 2;
    private static final int EVENT_TYPE_DOWN = 3;
    private static final int EVENT_TYPE_UP = 4;
    private static final int EVENT_TYPE_BUTTON_DOWN = 5;
    private static final int EVENT_TYPE_BUTTON_UP = 6;
    private static final int EVENT_TYPE_IN_RANGE = 7;
    private static final int EVENT_TYPE_OUT_OF_RANGE = 8;

    @Override
    public void pointerXYEvent(int deviceType, int pointerID, int eventType, boolean inverted, int x, int y, int pressure) {
        System.out.println("deviceType: " + deviceType);
        System.out.println("pointerID: " + pointerID);
        System.out.println("eventType: " + eventType);
        System.out.println("inverted: " + inverted);
        System.out.println("x, y: " + x + ", " + y);
        System.out.println("pressure: " + pressure);
        System.out.println();

        if (deviceType == 0){
            switch (eventType) {
                case EVENT_TYPE_DOWN :
                    realTouchDown(x, y, pointerID);
                    break;
                case EVENT_TYPE_DRAG :
                    realTouchDragged(x, y, pointerID);
                    break;
                case EVENT_TYPE_UP :
                    realTouchUp(x, y, pointerID);
                    break;
            }
        }
        //Point p = SwingUtilities.convertPoint(rootComponent, x, y, this);
        //x = p.x;
        //y = p.y;
        //System.out.println("Pointer coordinates: "+x+","+y);
    }

    @Override
    public void pointerButtonEvent(int i, int i1, int i2, boolean b, int i3) {

    }

    @Override
    public void pointerEvent(int i, int i1, int i2, boolean b) {

    }

	/*public boolean keyDown(int keyCode) {
		if (keyCode == Keys.W) {
			Vector2 f = m_body.getWorldVector(tmp.set(0, -200));
			Vector2 p = m_body.getWorldPoint(tmp.set(0, 2));
			m_body.applyForce(f, p, true);
		}
		if (keyCode == Keys.A) m_body.applyTorque(50, true);
		if (keyCode == Keys.D) m_body.applyTorque(-50, true);

		return false;
	}*/
}