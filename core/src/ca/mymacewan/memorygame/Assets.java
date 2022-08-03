package ca.mymacewan.memorygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;

public class Assets {
    public static final AssetManager manager = new AssetManager();

    public static final AssetDescriptor<ParticleEffect> particleEffect =
            new AssetDescriptor<ParticleEffect>(Gdx.files.internal("explosion.p"), ParticleEffect.class);
    public static final AssetDescriptor<Texture> cardBack =
            new AssetDescriptor<Texture>("cardBack.png", Texture.class);
    public static final AssetDescriptor<Texture> unmatchedTextureSheet =
            new AssetDescriptor<Texture>("unmatched.png", Texture.class);
    public static final AssetDescriptor<Texture> matchedTextureSheet =
            new AssetDescriptor<Texture>("matched.png", Texture.class);
    public static final AssetDescriptor<BitmapFont> font =
            new AssetDescriptor<BitmapFont>("KenPixelBlocks.fnt", BitmapFont.class);
    public static final AssetDescriptor<Sound> pairSound =
            new AssetDescriptor<Sound>("SoundEffects/match.mp3", Sound.class);
    public static final AssetDescriptor<Sound> turnOverSound =
            new AssetDescriptor<Sound>("SoundEffects/flip.wav", Sound.class);
    public static final AssetDescriptor<Sound> winSound =
            new AssetDescriptor<Sound>("SoundEffects/win.mp3", Sound.class);
    public static final AssetDescriptor<Sound> loseSound =
            new AssetDescriptor<Sound>("SoundEffects/lose.mp3", Sound.class);

    public static void load(){
        manager.load(particleEffect);
        manager.load(cardBack);
        manager.load(unmatchedTextureSheet);
        manager.load(matchedTextureSheet);
        manager.load(font);
        manager.load(pairSound);
        manager.load(turnOverSound);
        manager.load(winSound);
        manager.load(loseSound);
    }

    public static void dispose(){
        manager.dispose();
    }
}
