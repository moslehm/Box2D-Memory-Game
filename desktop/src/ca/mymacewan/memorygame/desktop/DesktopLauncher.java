package ca.mymacewan.memorygame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ca.mymacewan.memorygame.MemoryGameView;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 3840;
		config.height = 2160;
		new LwjglApplication(new MemoryGameView(), config);
	}
}
