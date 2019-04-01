package ca.mymacewan.memorygame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ca.mymacewan.memorygame.MemoryGameView;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1920; // Table: 3840 Tablet: 2048 Laptop: 1366
		config.height = 1080; // Table: 2160 Tablet: 1536 Laptop: 768
		config.samples = 4; //or 2 or 8 or 16
		new LwjglApplication(new MemoryGameView(), config);
	}
}
