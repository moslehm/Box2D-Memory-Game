package ca.mymacewan.memorygame;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Memory Game");
		Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
		config.setWindowedMode(displayMode.width, displayMode.height);
		config.setDecorated(false);
		config.setWindowIcon(Files.FileType.Internal, "Icons/32x32.png", "Icons/48x48.png", "Icons/64x64.png");
		new Lwjgl3Application(new MemoryGameView(), config);
	}
}
