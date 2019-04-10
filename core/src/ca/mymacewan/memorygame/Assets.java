package ca.mymacewan.memorygame;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class Assets {
    public static final AssetManager manager = new AssetManager();

    public static final AssetDescriptor<Texture> cardBack =
            new AssetDescriptor<Texture>("cardBack.png", Texture.class);
    public static final String demoCard = "cardFront";
    public static final AssetDescriptor<Texture> demoCard0 =
            new AssetDescriptor<Texture>("cardFront0.png", Texture.class);
    public static final AssetDescriptor<Texture> demoCard1 =
            new AssetDescriptor<Texture>("cardFront1.png", Texture.class);
    public static final AssetDescriptor<Texture> demoCard2 =
            new AssetDescriptor<Texture>("cardFront2.png", Texture.class);
    public static final AssetDescriptor<Texture> demoCard3 =
            new AssetDescriptor<Texture>("cardFront3.png", Texture.class);
    public static final AssetDescriptor<Texture> demoCard4 =
            new AssetDescriptor<Texture>("cardFront4.png", Texture.class);
    public static final AssetDescriptor<Texture> demoCard5 =
            new AssetDescriptor<Texture>("cardFront5.png", Texture.class);
    public static final AssetDescriptor<Texture> demoCard6 =
            new AssetDescriptor<Texture>("cardFront6.png", Texture.class);
    public static final AssetDescriptor<Texture> demoCard7 =
            new AssetDescriptor<Texture>("cardFront7.png", Texture.class);

    public static void load(){
        manager.load(cardBack);
        manager.load(demoCard0);
        manager.load(demoCard1);
        manager.load(demoCard2);
        manager.load(demoCard3);
        manager.load(demoCard4);
        manager.load(demoCard5);
        manager.load(demoCard6);
        manager.load(demoCard7);
    }

    public static void dispose(){
        manager.dispose();
    }
}
