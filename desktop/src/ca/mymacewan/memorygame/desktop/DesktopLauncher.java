package ca.mymacewan.memorygame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ca.mymacewan.memorygame.MemoryGameView;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1366;
		config.height = 768;
		config.samples = 4; //or 2 or 8 or 16
		new LwjglApplication(new MemoryGameView(), config);
	}
}
